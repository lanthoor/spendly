#!/usr/bin/env python3
"""
Test Data Generator for Spendly App
Generates 130 transactions per month (30 SMS + 100 DB) spread across 4 random days for the last 6 months

IMPORTANT REQUIREMENTS:
- Android SDK platform-tools (adb) must be in your PATH
- Emulator must use a "Google APIs" image (not "Google Play") for rooting
- sqlite3 must be available on the emulator
- SMS sending uses time-shift method for accurate timestamps (requires root)

STRATEGY:
For each day:
  1. Set device date to target day (10 AM)
  2. Kill the Spendly app
  3. Launch the Spendly app
  4. Send all SMS for that day (each SMS body contains the date in format "13-Dec-24")
  5. Wait for app to process SMS
  6. Repeat for next day

DEBUGGING:
If transactions are still showing current date instead of historical dates:
1. Check the debug output - it will show:
   - Device date after setting
   - SMS date embedded in message body
2. The SMS body MUST contain dates in format "13-Dec-24" (or "13Dec24" for SBI)
3. The Spendly app parser looks for this pattern in the SMS text
4. If the date pattern doesn't match, it falls back to SMS receipt timestamp
"""

import subprocess
import json
import random
import argparse
import sys
from datetime import datetime
from pathlib import Path
import time

# =============== CONFIGURATION ===============

# Use adb from PATH (ensure Android SDK platform-tools is in your PATH)
ADB_PATH = "adb"

PROJECT_DIR = Path(__file__).parent
DATA_DIR = PROJECT_DIR / "data"

# Transaction configuration (defaults)
TRANSACTIONS_PER_MONTH = 30
SMS_PER_MONTH = 30
DB_PER_MONTH = 100
DAYS_PER_MONTH = 4  # Spread SMS across 4 random days per month

# Base category distributions (will be randomized per month)
BASE_EXPENSE_CATEGORY_WEIGHTS = {
    1: 0.20,   # Food: 20%
    6: 0.18,   # Shopping: 18%
    12: 0.15,  # Groceries: 15%
    2: 0.12,   # Travel: 12%
    4: 0.08,   # Utilities: 8%
    7: 0.07,   # Media: 7%
    8: 0.05,   # Healthcare: 5%
    5: 0.05,   # Services: 5%
    3: 0.03,   # Rent: 3%
    10: 0.03,  # Education: 3%
    9: 0.02,   # Gifts: 2%
    11: 0.02,  # Investments: 2%
}

# Base income category distribution (will be randomized per month)
BASE_INCOME_CATEGORY_WEIGHTS = {
    101: 0.60,  # Salary: 60%
    102: 0.20,  # Freelance: 20%
    109: 0.10,  # Bonus: 10%
    108: 0.05,  # Refund: 5%
    106: 0.03,  # Interest: 3%
    103: 0.02,  # Business: 2%
}

# Amount ranges in rupees (will be converted to paise)
AMOUNT_RANGES = {
    1: (50, 1500),        # Food
    2: (30, 5000),        # Travel
    3: (8000, 25000),     # Rent
    4: (200, 3000),       # Utilities
    5: (300, 2000),       # Services
    6: (200, 20000),      # Shopping
    7: (99, 799),         # Media
    8: (100, 5000),       # Healthcare
    9: (500, 10000),      # Gifts
    10: (1000, 15000),    # Education
    11: (1000, 50000),    # Investments
    12: (100, 5000),      # Groceries
    13: (50, 2000),       # Others
    101: (45000, 60000),  # Salary
    102: (5000, 50000),   # Freelance
    103: (10000, 100000), # Business
    106: (100, 2000),     # Interest
    108: (100, 5000),     # Refund
    109: (5000, 50000),   # Bonus
}

# Account distribution
ACCOUNT_DISTRIBUTION = {
    "MY_ACCOUNT": 0.50,
    "CREDIT_CARD": 0.30,
    "DEBIT_CARD": 0.20,
}

# =============== DATA LOADING ===============

def load_merchants():
    """Load merchant data from JSON file"""
    with open(DATA_DIR / "merchants.json", "r") as f:
        return json.load(f)

def load_sms_templates():
    """Load SMS templates from JSON file"""
    with open(DATA_DIR / "sms_templates.json", "r") as f:
        return json.load(f)

MERCHANTS_DATA = load_merchants()
SMS_TEMPLATES_DATA = load_sms_templates()

# =============== UTILITY FUNCTIONS ===============

def run_adb_command(command, description="", check=True):
    """Execute ADB command and return output"""
    full_command = f"{ADB_PATH} {command}"
    try:
        result = subprocess.run(
            full_command,
            shell=True,
            capture_output=True,
            text=True,
            check=check
        )
        return result.stdout.strip()
    except subprocess.CalledProcessError as e:
        print(f"❌ Error executing: {description}")
        print(f"   Command: {full_command}")
        print(f"   Error: {e.stderr}")
        if check:
            sys.exit(1)
        return None


def format_amount_for_sms(amount_in_paise):
    """Format amount in Indian currency format for SMS (e.g., 1,234.50)"""
    amount_in_rupees = amount_in_paise / 100.0
    # Indian number formatting
    amount_str = f"{amount_in_rupees:,.2f}"
    return amount_str

def generate_random_amount(category_id):
    """Generate random amount in paise for given category"""
    min_amt, max_amt = AMOUNT_RANGES.get(category_id, (100, 5000))
    amount_in_rupees = random.randint(min_amt, max_amt)
    return amount_in_rupees * 100  # Convert to paise

def generate_random_account(account_ids):
    """Select random account based on distribution"""
    account_type = random.choices(
        list(ACCOUNT_DISTRIBUTION.keys()),
        weights=list(ACCOUNT_DISTRIBUTION.values())
    )[0]

    # Map to actual account ID
    account_id_map = {
        "MY_ACCOUNT": account_ids["MY_ACCOUNT"],
        "CREDIT_CARD": account_ids["CREDIT_CARD"],
        "DEBIT_CARD": account_ids["DEBIT_CARD"],
    }
    return account_id_map[account_type]

def generate_merchant(category_id, is_income=False):
    """Get random merchant for given category"""
    if is_income:
        category_data = MERCHANTS_DATA["income_categories"].get(str(category_id), {})
        merchants = category_data.get("sources", ["Payment"])
    else:
        category_data = MERCHANTS_DATA["expense_categories"].get(str(category_id), {})
        merchants = category_data.get("merchants", ["Purchase"])

    return random.choice(merchants)

def generate_description(category_id, merchant, is_income=False):
    """Generate description for transaction"""
    if is_income:
        category_data = MERCHANTS_DATA["income_categories"].get(str(category_id), {})
        descriptions = category_data.get("descriptions", [merchant])
    else:
        category_data = MERCHANTS_DATA["expense_categories"].get(str(category_id), {})
        descriptions = category_data.get("descriptions", [merchant])

    return random.choice(descriptions)

def generate_random_date(month, year, existing_dates):
    """Generate random datetime for given month, avoiding heavy clustering"""
    month_num = datetime.strptime(month, "%b").month
    days_in_month = 31 if month_num in [1,3,5,7,8,10,12] else 30 if month_num != 2 else 28

    # Try to find a date that's not too clustered
    max_attempts = 10
    for _ in range(max_attempts):
        day = random.randint(1, days_in_month)
        hour = random.randint(8, 23)
        minute = random.randint(0, 59)
        second = random.randint(0, 59)

        dt = datetime(year, month_num, day, hour, minute, second)

        # Check if this datetime is too close to existing ones (within 5 minutes)
        too_close = any(abs((dt - existing).total_seconds()) < 300 for existing in existing_dates)
        if not too_close:
            return dt

    # If all attempts failed, just return a random datetime
    day = random.randint(1, days_in_month)
    hour = random.randint(8, 23)
    minute = random.randint(0, 59)
    second = random.randint(0, 59)
    return datetime(year, month_num, day, hour, minute, second)

def generate_random_days(month, year, num_days):
    """Generate random days in a month for spreading transactions"""
    month_num = datetime.strptime(month, "%b").month
    days_in_month = 31 if month_num in [1,3,5,7,8,10,12] else 30 if month_num != 2 else 28

    # Generate unique random days
    selected_days = random.sample(range(1, days_in_month + 1), min(num_days, days_in_month))
    return sorted(selected_days)

def generate_random_time_for_day(year, month_num, day, existing_times):
    """Generate random time for a specific day, avoiding clustering"""
    max_attempts = 10
    for _ in range(max_attempts):
        hour = random.randint(8, 23)
        minute = random.randint(0, 59)
        second = random.randint(0, 59)

        dt = datetime(year, month_num, day, hour, minute, second)

        # Check if this time is too close to existing ones (within 2 minutes)
        too_close = any(abs((dt - existing).total_seconds()) < 120 for existing in existing_times)
        if not too_close:
            return dt

    # If all attempts failed, just return a random time
    hour = random.randint(8, 23)
    minute = random.randint(0, 59)
    second = random.randint(0, 59)
    return datetime(year, month_num, day, hour, minute, second)

def format_date_for_sms(dt, bank_key):
    """Format date based on bank requirements"""
    if bank_key == "SBIPSG":
        # SBI uses no dashes: 13Dec24
        # Ensure month is capitalized (e.g., "Dec" not "DEC" or "dec")
        month = dt.strftime("%b")  # Get abbreviated month name
        day = dt.strftime("%d")
        year = dt.strftime("%y")
        # Capitalize first letter, lowercase rest (matches parser regex)
        month = month[0].upper() + month[1:].lower()
        return f"{day}{month}{year}"
    else:
        # Most banks use: 13-Dec-24
        # Ensure month is capitalized (e.g., "Dec" not "DEC" or "dec")
        month = dt.strftime("%b")  # Get abbreviated month name
        day = dt.strftime("%d")
        year = dt.strftime("%y")
        # Capitalize first letter, lowercase rest (matches parser regex)
        month = month[0].upper() + month[1:].lower()
        return f"{day}-{month}-{year}"

def generate_reference_number():
    """Generate random UPI reference number"""
    return f"{random.randint(100000000000, 999999999999)}"

def generate_balance():
    """Generate random account balance for SMS"""
    return f"{random.randint(10000, 150000):,.2f}"

def generate_month_list(months_back, end_date=None):
    """Generate list of (month, year) tuples going back from end_date"""
    if end_date is None:
        end_date = datetime.now()

    month_list = []
    current_date = end_date

    for _ in range(months_back):
        month_list.append((current_date.strftime("%b"), current_date.year))
        # Go back one month
        if current_date.month == 1:
            current_date = current_date.replace(year=current_date.year - 1, month=12)
        else:
            # Handle day overflow (e.g., Jan 31 -> Feb 28)
            try:
                current_date = current_date.replace(month=current_date.month - 1)
            except ValueError:
                # Day doesn't exist in previous month (e.g., Jan 31 -> Feb 31)
                # Set to last day of previous month
                if current_date.month == 1:
                    current_date = current_date.replace(year=current_date.year - 1, month=12, day=28)
                else:
                    current_date = current_date.replace(month=current_date.month - 1, day=28)

    # Reverse to go chronologically (oldest first)
    return list(reversed(month_list))

def randomize_category_distribution(base_weights, total_count, variation=0.3):
    """
    Generate randomized category distribution based on base weights
    variation: 0.0-1.0, how much to vary from base (0.3 = ±30%)
    """
    distribution = {}

    # Add random variation to each weight
    varied_weights = {}
    for cat_id, base_weight in base_weights.items():
        # Random variation: ±variation% of base weight
        var_amount = base_weight * variation * (random.random() * 2 - 1)
        varied_weight = max(0.01, base_weight + var_amount)  # Ensure at least 1%
        varied_weights[cat_id] = varied_weight

    # Normalize weights to sum to 1.0
    total_weight = sum(varied_weights.values())
    normalized_weights = {k: v / total_weight for k, v in varied_weights.items()}

    # Convert to counts
    for cat_id, weight in normalized_weights.items():
        distribution[cat_id] = int(total_count * weight)

    # Adjust for rounding errors to ensure exact total
    actual_total = sum(distribution.values())
    diff = total_count - actual_total

    if diff > 0:
        # Add remaining to random categories
        for _ in range(diff):
            cat_id = random.choice(list(distribution.keys()))
            distribution[cat_id] += 1
    elif diff < 0:
        # Remove excess from random categories
        for _ in range(abs(diff)):
            cat_id = random.choice([k for k, v in distribution.items() if v > 1])
            distribution[cat_id] -= 1

    return distribution

# =============== SMS GENERATION ===============

def generate_sms_message(bank_key, is_expense, amount_in_paise, merchant, date, account_hint, balance, source=None):
    """Generate SMS message based on bank and transaction type"""
    bank_data = SMS_TEMPLATES_DATA["banks"][bank_key]

    # Select template
    templates = bank_data["expense_templates"] if is_expense else bank_data["income_templates"]
    template = random.choice(templates)

    # Format amount
    amount_str = format_amount_for_sms(amount_in_paise)

    # Format date
    date_str = format_date_for_sms(date, bank_key)
    date_nosep = date.strftime("%d%b%y")

    # Generate description
    description = merchant if is_expense else (source if source else merchant)

    # Generate reference for UPI
    ref = generate_reference_number()

    # Format message
    message = template.format(
        amount=amount_str,
        merchant=merchant,
        date=date_str,
        date_nosep=date_nosep,
        account=account_hint,
        balance=balance,
        description=description,
        source=source if source else merchant,
        ref=ref
    )

    # Select random sender variant
    senders = SMS_TEMPLATES_DATA["senders"][bank_key]
    sender = random.choice(senders)

    return sender, message

def generate_sms_transactions(month, year, count, account_ids, days_per_month):
    """Generate SMS transactions for a month, grouped by day"""
    month_num = datetime.strptime(month, "%b").month

    # Select random days for this month
    selected_days = generate_random_days(month, year, days_per_month)

    # Distribute count across days
    sms_per_day = count // len(selected_days)
    remainder = count % len(selected_days)

    # Group SMS by day
    sms_by_day = {}

    # Calculate expense/income split (85% expense, 15% income)
    expense_count = int(count * 0.85)
    income_count = count - expense_count

    # Generate randomized distributions for this month
    expense_distribution = randomize_category_distribution(
        BASE_EXPENSE_CATEGORY_WEIGHTS, expense_count, variation=0.3
    )
    income_distribution = randomize_category_distribution(
        BASE_INCOME_CATEGORY_WEIGHTS, income_count, variation=0.3
    )

    # Get bank distribution
    banks = list(SMS_TEMPLATES_DATA["banks"].keys())
    weights = [SMS_TEMPLATES_DATA["banks"][b]["weight"] for b in banks]

    # Generate all transactions first
    all_transactions = []

    # Generate expenses
    for _ in range(expense_count):
        bank_key = random.choices(banks, weights=weights)[0]
        category_id = random.choices(
            list(expense_distribution.keys()),
            weights=list(expense_distribution.values())
        )[0]

        amount = generate_random_amount(category_id)
        merchant = generate_merchant(category_id, is_income=False)

        account_patterns = SMS_TEMPLATES_DATA["banks"][bank_key]["account_patterns"]
        account_hint = random.choice(account_patterns) if account_patterns else "1234"
        balance = generate_balance()

        all_transactions.append({
            "type": "expense",
            "bank_key": bank_key,
            "category_id": category_id,
            "amount": amount,
            "merchant": merchant,
            "account_hint": account_hint,
            "balance": balance,
        })

    # Generate income
    for _ in range(income_count):
        bank_key = random.choices(banks, weights=weights)[0]
        category_id = random.choices(
            list(income_distribution.keys()),
            weights=list(income_distribution.values())
        )[0]

        amount = generate_random_amount(category_id)
        source = generate_merchant(category_id, is_income=True)

        account_patterns = SMS_TEMPLATES_DATA["banks"][bank_key]["account_patterns"]
        account_hint = random.choice(account_patterns) if account_patterns else "1234"
        balance = generate_balance()

        all_transactions.append({
            "type": "income",
            "bank_key": bank_key,
            "category_id": category_id,
            "amount": amount,
            "merchant": source,
            "source": source,
            "account_hint": account_hint,
            "balance": balance,
        })

    # Shuffle transactions
    random.shuffle(all_transactions)

    # Distribute transactions across selected days
    txn_index = 0
    for day_idx, day in enumerate(selected_days):
        # Calculate how many SMS for this day
        day_count = sms_per_day + (1 if day_idx < remainder else 0)

        day_sms = []
        existing_times = []

        for _ in range(day_count):
            if txn_index >= len(all_transactions):
                break

            txn = all_transactions[txn_index]
            txn_index += 1

            # Generate time for this transaction
            date = generate_random_time_for_day(year, month_num, day, existing_times)
            existing_times.append(date)

            # Generate SMS
            if txn["type"] == "expense":
                sender, message = generate_sms_message(
                    txn["bank_key"], True, txn["amount"], txn["merchant"],
                    date, txn["account_hint"], txn["balance"]
                )
            else:
                sender, message = generate_sms_message(
                    txn["bank_key"], False, txn["amount"], txn["merchant"],
                    date, txn["account_hint"], txn["balance"], source=txn["source"]
                )

            timestamp_ms = int(date.timestamp() * 1000)
            day_sms.append((sender, message, timestamp_ms))

        # Sort by timestamp within the day
        day_sms.sort(key=lambda x: x[2])

        # Store with day as key
        day_date = datetime(year, month_num, day)
        sms_by_day[day_date] = day_sms

    return sms_by_day

# =============== DATABASE TRANSACTION GENERATION ===============

def generate_db_transactions(month, year, count, account_ids):
    """Generate database transactions for a month"""
    transactions = []
    existing_dates = []

    # Calculate expense/income split (90% expense, 10% income)
    expense_count = int(count * 0.90)
    income_count = count - expense_count

    # Generate randomized distributions for this month
    expense_distribution = randomize_category_distribution(
        BASE_EXPENSE_CATEGORY_WEIGHTS, expense_count, variation=0.3
    )
    income_distribution = randomize_category_distribution(
        BASE_INCOME_CATEGORY_WEIGHTS, income_count, variation=0.3
    )

    # Generate expenses
    for _ in range(expense_count):
        # Select category weighted by randomized distribution
        category_id = random.choices(
            list(expense_distribution.keys()),
            weights=list(expense_distribution.values())
        )[0]

        # Generate details
        amount = generate_random_amount(category_id)
        merchant = generate_merchant(category_id, is_income=False)
        description = generate_description(category_id, merchant, is_income=False)
        date = generate_random_date(month, year, existing_dates)
        existing_dates.append(date)
        account_id = generate_random_account(account_ids)

        timestamp_ms = int(date.timestamp() * 1000)
        created_at = int(time.time() * 1000)

        transactions.append({
            "type": "expense",
            "amount": amount,
            "category_id": category_id,
            "date": timestamp_ms,
            "description": description,
            "account_id": account_id,
            "created_at": created_at,
            "modified_at": created_at,
        })

    # Generate income
    for _ in range(income_count):
        # Select income category weighted by randomized distribution
        category_id = random.choices(
            list(income_distribution.keys()),
            weights=list(income_distribution.values())
        )[0]

        # Generate details
        amount = generate_random_amount(category_id)
        source = generate_merchant(category_id, is_income=True)
        description = generate_description(category_id, source, is_income=True)
        date = generate_random_date(month, year, existing_dates)
        existing_dates.append(date)
        account_id = generate_random_account(account_ids)

        timestamp_ms = int(date.timestamp() * 1000)
        created_at = int(time.time() * 1000)

        # Determine income source enum (must match IncomeSource enum in Enums.kt)
        # Valid values: SALARY, FREELANCE, INVESTMENT, GIFTS, REFUND, BUSINESS, RENTAL, INTEREST, BONUS, OTHER
        # Category ID to IncomeSource mapping:
        if category_id == 101:
            source_enum = "SALARY"
        elif category_id == 102:
            source_enum = "FREELANCE"
        elif category_id == 103:
            source_enum = "BUSINESS"
        elif category_id == 106:
            source_enum = "INTEREST"
        elif category_id == 108:
            source_enum = "REFUND"
        elif category_id == 109:
            source_enum = "BONUS"  # Bonus/Incentive category
        elif category_id == 11:
            source_enum = "INVESTMENT"  # Investment category
        elif category_id == 9:
            source_enum = "GIFTS"  # Gift Received category
        elif category_id == 3:
            source_enum = "RENTAL"  # Rental Income category
        else:
            source_enum = "OTHER"

        transactions.append({
            "type": "income",
            "amount": amount,
            "category_id": category_id,
            "source": source_enum,
            "date": timestamp_ms,
            "description": description,
            "account_id": account_id,
            "created_at": created_at,
            "modified_at": created_at,
        })

    # Sort by timestamp
    transactions.sort(key=lambda x: x["date"])

    return transactions

# =============== SMS SENDING WITH TIME-SHIFT ===============

def root_device():
    """Root the device/emulator for time manipulation"""
    print("\n🔓 Rooting device for time manipulation...")
    result = run_adb_command("root", "Root device", check=False)
    if result is None:
        print("   ⚠️  Warning: Failed to root device. SMS timestamps may not work correctly.")
        print("   Make sure you're using a 'Google APIs' emulator image, not 'Google Play'.")
        return False
    time.sleep(2)  # Wait for root to take effect
    print("   ✅ Device rooted")
    return True

def disable_auto_time():
    """Disable automatic time synchronization"""
    print("   Disabling automatic time sync...")
    run_adb_command("shell settings put global auto_time 0", check=False)
    run_adb_command("shell settings put global auto_time_zone 0", check=False)

def enable_auto_time():
    """Re-enable automatic time synchronization"""
    print("\n🕐 Re-enabling automatic time sync...")
    run_adb_command("shell settings put global auto_time 1", check=False)
    run_adb_command("shell settings put global auto_time_zone 1", check=False)

def set_device_date(target_date):
    """Set the device/emulator system time to a specific date (10 AM)"""
    # Set to 10 AM of the target date
    dt = datetime(target_date.year, target_date.month, target_date.day, 10, 0, 0)
    # Format: MMDDhhmm[[CC]YY][.ss]
    date_str = dt.strftime("%m%d%H%M%Y.%S")
    run_adb_command(f"shell date {date_str}", "Set device date", check=False)
    # Broadcast time change to notify apps
    run_adb_command("shell am broadcast -a android.intent.action.TIME_SET", check=False)
    time.sleep(2)  # Wait longer for time to take effect

    # Verify the date was set correctly
    verify_date = run_adb_command("shell date", "Verify device date", check=False)
    if verify_date:
        print(f"         Device date verified: {verify_date}")

def get_current_host_time():
    """Get current host system time in emulator date format"""
    return datetime.now().strftime("%m%d%H%M%Y.%S")

def reset_device_time():
    """Reset device time to current host time"""
    current_time = get_current_host_time()
    run_adb_command(f"shell date {current_time}", check=False)
    run_adb_command("shell am broadcast -a android.intent.action.TIME_SET", check=False)

def kill_app():
    """Force stop the Spendly app"""
    run_adb_command("shell am force-stop in.co.spendly", "Kill app", check=False)
    time.sleep(1)

def launch_app():
    """Launch the Spendly app"""
    run_adb_command("shell am start -n in.co.spendly/.MainActivity", "Launch app", check=False)
    time.sleep(5)  # Wait longer for app to fully initialize and register SMS receiver

def send_sms_simple(sender, message, debug=False):
    """Send single SMS via ADB without time manipulation"""
    if debug:
        # Extract date from message for debugging
        import re
        date_match = re.search(r'(\d{2}-\w{3}-\d{2})', message)
        if date_match:
            print(f"         SMS contains date: {date_match.group(1)}")
        else:
            print(f"         WARNING: No date found in SMS body!")

    # Escape single quotes in message
    escaped_message = message.replace("'", "'\\''")
    command = f"emu sms send {sender} '{escaped_message}'"
    run_adb_command(command, check=False)
    time.sleep(0.3)  # Brief delay between SMS

def send_sms_by_day(sms_by_day):
    """Send SMS messages grouped by day, restarting app for each day"""
    total_days = len(sms_by_day)
    total_sms = sum(len(sms_list) for sms_list in sms_by_day.values())

    print(f"\n📱 Sending {total_sms} SMS messages across {total_days} days...")
    print("   Strategy: Set date → Kill app → Launch app → Send SMS for that day")

    # Root device and disable auto time
    if not root_device():
        print("   ⚠️  Warning: Continuing without root. Timestamps may be incorrect.")
    disable_auto_time()

    # Sort days chronologically
    sorted_days = sorted(sms_by_day.keys())

    total_sent = 0
    total_failed = 0

    for day_idx, day_date in enumerate(sorted_days, 1):
        sms_list = sms_by_day[day_date]
        day_str = day_date.strftime("%Y-%m-%d")

        print(f"\n   📅 Day {day_idx}/{total_days}: {day_str} ({len(sms_list)} messages)")

        # 1. Set device date
        print(f"      Setting device date to {day_str}...")
        set_device_date(day_date)

        # 2. Kill app
        print(f"      Killing app...")
        kill_app()

        # 3. Launch app
        print(f"      Launching app...")
        launch_app()

        # 4. Send all SMS for this day
        print(f"      Sending {len(sms_list)} SMS...")
        for idx, (sender, message, timestamp_ms) in enumerate(sms_list):
            try:
                # Debug first SMS of each day
                debug = (idx == 0)
                if debug:
                    sms_date = datetime.fromtimestamp(timestamp_ms / 1000)
                    print(f"         First SMS timestamp: {sms_date.strftime('%Y-%m-%d %H:%M:%S')}")
                send_sms_simple(sender, message, debug=debug)
                total_sent += 1
            except Exception as e:
                print(f"         ❌ Failed: {str(e)[:50]}")
                total_failed += 1

        # 5. Wait for app to process
        print(f"      Waiting for app to process SMS...")
        time.sleep(5)  # Wait longer to ensure all SMS are processed

    # Reset device time to current time
    print("\n🕐 Resetting device time to current...")
    reset_device_time()
    enable_auto_time()

    print(f"\n   ✅ Total Sent: {total_sent}, ❌ Total Failed: {total_failed}")
    return total_sent, total_failed

# =============== DATABASE INSERTION ===============

def inject_transaction_batch(transactions, batch_size=100):
    """Inject transactions into database in batches"""
    total = len(transactions)
    inserted = 0
    failed = 0

    print(f"\n💾 Inserting {total} database transactions...")

    # Process in batches using heredoc to avoid escaping issues
    for i in range(0, total, batch_size):
        batch = transactions[i:i+batch_size]

        # Build SQL statements for batch
        sql_lines = ["BEGIN TRANSACTION;"]

        for txn in batch:
            # Escape single quotes in description by doubling them (SQL standard)
            description = txn['description'].replace("'", "''")

            if txn["type"] == "expense":
                sql = f"INSERT INTO expenses (amount, category_id, date, description, account_id, created_at, modified_at, sms_source_id, sms_body, sms_confidence, sms_timestamp) VALUES ({txn['amount']}, {txn['category_id']}, {txn['date']}, '{description}', {txn['account_id']}, {txn['created_at']}, {txn['modified_at']}, NULL, NULL, NULL, NULL);"
            else:
                source = txn['source'].replace("'", "''")
                sql = f"INSERT INTO income (amount, category_id, source, date, description, account_id, is_recurring, linked_expense_id, created_at, modified_at, sms_source_id, sms_body, sms_confidence, sms_timestamp) VALUES ({txn['amount']}, {txn['category_id']}, '{source}', {txn['date']}, '{description}', {txn['account_id']}, 0, NULL, {txn['created_at']}, {txn['modified_at']}, NULL, NULL, NULL, NULL);"

            sql_lines.append(sql)

        sql_lines.append("COMMIT;")
        full_sql = "\n".join(sql_lines)

        # Use heredoc to pass SQL without shell escaping issues
        heredoc_command = f"""shell << 'EOF'
run-as in.co.spendly sqlite3 /data/data/in.co.spendly/databases/spendly_database << 'SQL'
{full_sql}
SQL
EOF
"""

        # Execute batch using subprocess with shell=True and stdin
        try:
            process = subprocess.run(
                f"{ADB_PATH} {heredoc_command}",
                shell=True,
                capture_output=True,
                text=True,
                timeout=30
            )

            if process.returncode == 0:
                inserted += len(batch)
                print(f"   Batch {i//batch_size + 1}/{(total + batch_size - 1)//batch_size} ✅ ({len(batch)} transactions)")
            else:
                failed += len(batch)
                print(f"   Batch {i//batch_size + 1}/{(total + batch_size - 1)//batch_size} ❌ Error: {process.stderr[:100]}")
        except Exception as e:
            failed += len(batch)
            print(f"   Batch {i//batch_size + 1}/{(total + batch_size - 1)//batch_size} ❌ Exception: {str(e)[:100]}")

    print(f"   ✅ Total Inserted: {inserted}, ❌ Total Failed: {failed}")
    return inserted

# =============== SETUP & INITIALIZATION ===============

def verify_adb_connection():
    """Verify ADB is connected to device"""
    print("🔌 Verifying ADB connection...")
    result = run_adb_command("devices", "Check ADB devices")

    if "emulator" not in result and "device" not in result:
        print("❌ No device/emulator connected. Please start the emulator first.")
        sys.exit(1)

    print("   ✅ Device connected")

def grant_permissions():
    """Grant required permissions to app"""
    print("\n🔐 Granting permissions...")

    # Core SMS permissions (required)
    required_permissions = [
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_SMS",
    ]

    # Optional permissions (may not exist on older Android versions)
    optional_permissions = [
        "android.permission.POST_NOTIFICATIONS",  # Android 13+ only
    ]

    # Grant required permissions
    for perm in required_permissions:
        run_adb_command(f"shell pm grant in.co.spendly {perm}", f"Grant {perm}")

    # Grant optional permissions (ignore errors)
    for perm in optional_permissions:
        result = run_adb_command(f"shell pm grant in.co.spendly {perm}", f"Grant {perm}", check=False)
        if result is None:
            print(f"   ⚠️  Skipped {perm} (not available on this Android version)")

    print("   ✅ Permissions granted")

def setup_app():
    """Initial app setup"""
    print("\n🚀 Setting up Spendly app...")
    # Launch once to ensure app is installed and initialized
    run_adb_command("shell am start -n in.co.spendly/.MainActivity", "Launch app")
    print("   ⏳ Waiting for app initialization...")
    time.sleep(5)
    run_adb_command("shell am force-stop in.co.spendly", "Stop app", check=False)
    print("   ✅ App setup complete")

def create_custom_accounts():
    """Create custom accounts (Credit Card, Debit Card) if they don't exist"""
    print("\n💳 Creating custom accounts...")

    timestamp = int(time.time() * 1000)

    # SQL to create accounts if they don't exist
    sql_statements = ["BEGIN TRANSACTION;"]

    # Credit Card account
    sql_statements.append(
        f"INSERT OR IGNORE INTO accounts (name, type, icon, color, is_custom, sort_order, created_at, modified_at) "
        f"VALUES ('Credit Card', 'CARD', 'creditcard', -65536, 1, 2, {timestamp}, {timestamp});"
    )

    # Debit Card account
    sql_statements.append(
        f"INSERT OR IGNORE INTO accounts (name, type, icon, color, is_custom, sort_order, created_at, modified_at) "
        f"VALUES ('Debit Card', 'CARD', 'creditcard', -16776961, 1, 3, {timestamp}, {timestamp});"
    )

    sql_statements.append("COMMIT;")
    full_sql = "\n".join(sql_statements)

    # Execute using heredoc
    heredoc_command = f"""shell << 'EOF'
run-as in.co.spendly sqlite3 /data/data/in.co.spendly/databases/spendly_database << 'SQL'
{full_sql}
SQL
EOF
"""

    try:
        process = subprocess.run(
            f"{ADB_PATH} {heredoc_command}",
            shell=True,
            capture_output=True,
            text=True,
            timeout=30
        )

        if process.returncode == 0:
            print("   ✅ Custom accounts created")
        else:
            print(f"   ⚠️  Warning: {process.stderr[:100]}")
    except Exception as e:
        print(f"   ⚠️  Warning: {str(e)[:100]}")

def query_account_ids():
    """Query account IDs from database"""
    print("\n🔍 Querying account IDs...")

    # Query My Account
    my_account_result = run_adb_command(
        'shell "run-as in.co.spendly sqlite3 /data/data/in.co.spendly/databases/spendly_database \\"SELECT id FROM accounts WHERE name=\'My Account\'\\"\"',
        "Query My Account ID",
        check=False
    )

    # Query Credit Card
    credit_card_result = run_adb_command(
        'shell "run-as in.co.spendly sqlite3 /data/data/in.co.spendly/databases/spendly_database \\"SELECT id FROM accounts WHERE name=\'Credit Card\'\\"\"',
        "Query Credit Card ID",
        check=False
    )

    # Query Debit Card
    debit_card_result = run_adb_command(
        'shell "run-as in.co.spendly sqlite3 /data/data/in.co.spendly/databases/spendly_database \\"SELECT id FROM accounts WHERE name=\'Debit Card\'\\"\"',
        "Query Debit Card ID",
        check=False
    )

    account_ids = {
        "MY_ACCOUNT": int(my_account_result.strip()) if my_account_result and my_account_result.strip().isdigit() else 1,
        "CREDIT_CARD": int(credit_card_result.strip()) if credit_card_result and credit_card_result.strip().isdigit() else 1,  # Fallback to My Account
        "DEBIT_CARD": int(debit_card_result.strip()) if debit_card_result and debit_card_result.strip().isdigit() else 1,  # Fallback to My Account
    }

    print(f"   My Account ID: {account_ids['MY_ACCOUNT']}")
    print(f"   Credit Card ID: {account_ids['CREDIT_CARD']}")
    print(f"   Debit Card ID: {account_ids['DEBIT_CARD']}")

    return account_ids

# =============== MAIN EXECUTION ===============

def main():
    parser = argparse.ArgumentParser(description="Generate test data for Spendly app")
    parser.add_argument("--months", type=int, default=6, help="Number of months to generate (default: 6)")
    parser.add_argument("--sms-only", action="store_true", help="Only send SMS, skip DB injection")
    parser.add_argument("--db-only", action="store_true", help="Only inject DB, skip SMS")
    parser.add_argument("--dry-run", action="store_true", help="Generate data without executing")
    parser.add_argument("--output", type=str, help="Export generated data to JSON file")
    parser.add_argument("--end-date", type=str, help="End date for generation (YYYY-MM-DD, default: today)")
    parser.add_argument("--variation", type=float, default=0.3, help="Spending pattern variation (0.0-1.0, default: 0.3)")

    args = parser.parse_args()

    # Validate months
    if args.months < 1:
        print("❌ Months must be at least 1")
        sys.exit(1)

    # Validate variation
    if args.variation < 0.0 or args.variation > 1.0:
        print("❌ Variation must be between 0.0 and 1.0")
        sys.exit(1)

    # Parse end date
    if args.end_date:
        try:
            end_date = datetime.strptime(args.end_date, "%Y-%m-%d")
        except ValueError:
            print("❌ Invalid end date format. Use YYYY-MM-DD")
            sys.exit(1)
    else:
        end_date = datetime.now()

    # Generate month list
    selected_months = generate_month_list(args.months, end_date)

    print("=" * 60)
    print("🎯 SPENDLY TEST DATA GENERATOR")
    print("=" * 60)
    first_month, first_year = selected_months[0]
    last_month, last_year = selected_months[-1]
    print(f"Period: {first_month} {first_year} - {last_month} {last_year} ({len(selected_months)} months)")
    print(f"Transactions per month: {TRANSACTIONS_PER_MONTH}")
    print(f"  - SMS: {SMS_PER_MONTH} (spread across {DAYS_PER_MONTH} random days)")
    print(f"  - Database: {DB_PER_MONTH}")
    print(f"Total SMS days: {len(selected_months) * DAYS_PER_MONTH}")
    print(f"Total transactions: {len(selected_months) * TRANSACTIONS_PER_MONTH}")
    print(f"Spending variation: ±{args.variation * 100:.0f}% per month")
    print("\nStrategy: For each day, set device date → kill app → launch app → send SMS")
    print("=" * 60)

    if args.dry_run:
        print("\n🏃 DRY RUN MODE - No execution, only data generation")

    # Setup
    if not args.dry_run:
        verify_adb_connection()
        grant_permissions()
        setup_app()
        create_custom_accounts()  # Create Credit Card and Debit Card accounts
        account_ids = query_account_ids()
    else:
        account_ids = {"MY_ACCOUNT": 1, "CREDIT_CARD": 2, "DEBIT_CARD": 3}

    # Generate data for all months
    all_sms_by_day = {}
    all_db_transactions = []

    for month, year in selected_months:
        print(f"\n{'='*60}")
        print(f"📅 Generating data for {month} {year}")
        print(f"{'='*60}")

        if not args.db_only:
            print(f"   Generating {SMS_PER_MONTH} SMS messages across {DAYS_PER_MONTH} days...")
            sms_by_day = generate_sms_transactions(month, year, SMS_PER_MONTH, account_ids, DAYS_PER_MONTH)
            # Merge into all_sms_by_day
            all_sms_by_day.update(sms_by_day)
            total_generated = sum(len(sms_list) for sms_list in sms_by_day.values())
            print(f"   ✅ Generated {total_generated} SMS messages")

        if not args.sms_only:
            print(f"   Generating {DB_PER_MONTH} database transactions...")
            db_txns = generate_db_transactions(month, year, DB_PER_MONTH, account_ids)
            all_db_transactions.extend(db_txns)
            print(f"   ✅ Generated {len(db_txns)} database transactions")

    # Flatten SMS for export
    all_sms_flat = []
    for day_date, sms_list in sorted(all_sms_by_day.items()):
        all_sms_flat.extend(sms_list)

    # Export if requested
    if args.output:
        print(f"\n💾 Exporting data to {args.output}...")
        first_month, first_year = selected_months[0]
        last_month, last_year = selected_months[-1]
        export_data = {
            "metadata": {
                "generated_at": datetime.now().isoformat(),
                "months": [{"month": m, "year": y} for m, y in selected_months],
                "period": f"{first_month} {first_year} - {last_month} {last_year}",
                "total_months": len(selected_months),
                "total_sms": len(all_sms_flat),
                "total_db_transactions": len(all_db_transactions),
                "variation": args.variation,
                "days_per_month": DAYS_PER_MONTH,
            },
            "sms": [{"sender": s, "message": m, "timestamp": t} for s, m, t in all_sms_flat],
            "db_transactions": all_db_transactions,
        }

        with open(args.output, "w") as f:
            json.dump(export_data, f, indent=2)

        print(f"   ✅ Exported to {args.output}")

    # Execute if not dry run
    if not args.dry_run:
        # Send SMS (with time-shift and app restart for accurate timestamps)
        if not args.db_only and all_sms_by_day:
            sent, failed = send_sms_by_day(all_sms_by_day)

        # Inject DB transactions
        if not args.sms_only and all_db_transactions:
            inserted = inject_transaction_batch(all_db_transactions, batch_size=100)

    # Print summary
    print("\n" + "=" * 60)
    print("✅ TEST DATA GENERATION COMPLETED!")
    print("=" * 60)
    print(f"📊 Summary:")
    first_month, first_year = selected_months[0]
    last_month, last_year = selected_months[-1]
    print(f"  - Months: {len(selected_months)} ({first_month} {first_year} - {last_month} {last_year})")
    print(f"  - Total Days: {len(all_sms_by_day)}")
    print(f"  - Total SMS: {len(all_sms_flat)}")
    print(f"  - Total DB Transactions: {len(all_db_transactions)}")
    print(f"  - Grand Total: {len(all_sms_flat) + len(all_db_transactions)}")
    print("=" * 60)
    print("\n📱 Open Spendly app to see all transactions!")

    if args.dry_run:
        print("\n💡 Run without --dry-run to execute on device")

if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        print("\n\n⚠️  Operation cancelled by user")
        sys.exit(0)
    except Exception as e:
        print(f"\n\n❌ Unexpected error: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)
