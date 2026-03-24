# Test Data Generator for Spendly

Python script to generate realistic test data for the Spendly expense tracking app. Generates 1500 transactions per month (500 SMS + 1000 database entries) for any time period you want.

## Quick Start

```bash
# Run full generation (9000 transactions over 6 months)
python3 test-input.py

# Generate 2 years of data (36,000 transactions)
python3 test-input.py --months 24

# Generate 3 years ending on specific date (54,000 transactions)
python3 test-input.py --months 36 --end-date 2025-12-31

# More random spending patterns (±50% variation)
python3 test-input.py --months 12 --variation 0.5

# Dry run to see what will be generated
python3 test-input.py --dry-run --output preview.json
```

## Usage

```
python3 test-input.py [options]

Options:
  --months N          Number of months to generate (default: 6, no limit)
  --end-date DATE     End date for generation (YYYY-MM-DD, default: today)
  --variation N       Spending pattern randomness 0.0-1.0 (default: 0.3)
  --sms-only          Only send SMS, skip database injection
  --db-only           Only inject database, skip SMS
  --dry-run           Generate data without executing (preview mode)
  --output FILE       Export generated data to JSON file
```

## Examples

```bash
# Generate last 3 months
python3 test-input.py --months 3

# Generate 18 months (1.5 years)
python3 test-input.py --months 18

# Generate data from Jan 2023 to Dec 2025 (3 years)
python3 test-input.py --months 36 --end-date 2025-12-31

# High variation in spending patterns (±50%)
python3 test-input.py --months 12 --variation 0.5

# Low variation for consistent spending (±10%)
python3 test-input.py --months 6 --variation 0.1

# Only send SMS messages (no DB injection)
python3 test-input.py --sms-only

# Only inject database transactions (no SMS)
python3 test-input.py --db-only

# Preview what will be generated
python3 test-input.py --dry-run --months 1

# Generate and export data to JSON
python3 test-input.py --months 2 --output data.json
```

## What Gets Generated

### Per Month (1500 transactions)
- **500 SMS messages** (85% expenses, 15% income)
  - Distributed across HDFC (35%), ICICI (25%), SBI (15%), UPI providers (15%), Others (10%)
  - All SMS messages are valid and parseable (100% success rate)
  - Realistic Indian bank SMS formats

- **1000 Database transactions** (90% expenses, 10% income)
  - Weighted by real-world category frequency
  - Distributed across 3 accounts: My Account (50%), Credit Card (30%), Debit Card (20%)

### Scalable
- **No time limit** - Generate data going back as far as you want
- **6 months (default)**: 9,000 transactions
- **12 months**: 18,000 transactions
- **24 months**: 36,000 transactions
- **36 months**: 54,000 transactions
- **Any period**: 1,500 transactions × number of months

## Data Distribution

### Expense Categories (by frequency)
- Food (20%) - Swiggy, Zomato, McDonald's, etc.
- Shopping (18%) - Amazon, Flipkart, Myntra, etc.
- Groceries (15%) - BigBasket, DMart, etc.
- Travel (12%) - Uber, Ola, IRCTC, etc.
- Utilities (8%) - Electricity, Internet, Mobile recharges
- Media (7%) - Netflix, Spotify, BookMyShow, etc.
- Healthcare (5%) - Apollo Pharmacy, hospitals, etc.
- Services (5%) - Urban Company, salons, gym, etc.
- Rent (3%) - Monthly fixed payments
- Education (3%) - School fees, courses, etc.
- Gifts (2%) - Birthday, festival gifts, etc.
- Investments (2%) - Stocks, mutual funds, insurance

### Income Categories
- Salary (60%) - Monthly salary credits
- Freelance (20%) - Project payments
- Bonus (10%) - Performance, festival bonuses
- Refund (5%) - Cashbacks, returns
- Interest (3%) - Bank interest
- Business (2%) - Business revenue

### SMS Banks
- HDFC Bank (35%) - Most realistic formats
- ICICI Bank (25%)
- SBI (15%)
- PhonePe (5%)
- PayTM (5%)
- Google Pay (5%)
- Axis Bank (5%)
- Kotak Bank (5%)

## Performance

- **SMS Generation**: ~2 seconds (3000 messages)
- **SMS Sending**: ~6 minutes (parallel, 10 workers, 50ms delay)
- **DB Generation**: ~1 second (6000 transactions)
- **DB Insertion**: ~1-2 minutes (batches of 100)
- **Total Time**: ~8-10 minutes for full 6 months

## Data Files

- `data/merchants.json` - 50+ merchants per category (14 categories)
- `data/sms_templates.json` - Bank-specific SMS templates (8 banks)

## Requirements

- Python 3.7+
- Android emulator running with Spendly app installed
- ADB (Android Debug Bridge) in PATH or at `~/Library/Android/sdk/platform-tools/adb`
- Optional: `tqdm` package for progress bars (`pip3 install tqdm`)

## Before Running

1. Start Android emulator
2. Install Spendly app on emulator
3. Verify ADB connection: `adb devices`
4. Run the script

## Data Quality Features

- **No clustering**: Transactions spread throughout each month
- **Random spending patterns**: Each month has ±30% variation in category distribution (configurable)
- **Realistic amounts**: Category-appropriate price ranges
- **Variety**: 100+ unique merchants, diverse descriptions
- **Duplicate prevention**: No identical SMS within 5 minutes
- **Timestamp distribution**: Random hours between 8 AM - 11 PM
- **Indian formats**: Currency (₹1,23,456.78), dates (DD-MMM-YY)

### Spending Pattern Variation

The `--variation` parameter controls how much spending patterns vary month-to-month:

- **0.0** (0%): Perfectly consistent spending every month
- **0.3** (30%, default): Realistic variation - some months spend more on food, others on shopping
- **0.5** (50%): High variation - spending varies significantly month to month
- **1.0** (100%): Maximum randomness - very unpredictable spending

Example: With base Food spending at 20% and variation 0.3:
- Month 1: Food might be 14% (20% - 30% of 20%)
- Month 2: Food might be 26% (20% + 30% of 20%)
- Month 3: Food might be 18%

This creates realistic data where spending habits change over time.

## Troubleshooting

**"No device/emulator connected"**
- Start Android emulator first
- Check with `adb devices`

**"Failed to send SMS"**
- Check emulator is responding
- Try reducing parallel workers: edit `max_workers=10` to lower value
- Check emulator SMS app is working

**"Database insertion failed"**
- Ensure app is installed and initialized
- Check run-as permissions
- Verify database exists

**Script is slow**
- This is expected for 3000+ SMS (network latency to emulator)
- Use `--db-only` to skip SMS if not needed
- Use `--months 1` for quicker testing

## Output

The script displays:
- Setup progress (permissions, app launch, account IDs)
- Generation progress (SMS and DB transactions)
- Sending/insertion progress (with counts)
- Final summary (total transactions by type)

Example output:
```
🎯 SPENDLY TEST DATA GENERATOR
Period: Jul-Dec 2025
Total transactions: 9000
  - SMS: 3000
  - Database: 6000

📱 Sending 3000 SMS messages...
   Batch 1/60 (50 messages)
   ...
   ✅ Sent: 2998, ❌ Failed: 2

💾 Inserting 6000 database transactions...
   Batch 1/60 ✅ (100 transactions)
   ...
   ✅ Inserted: 6000

✅ TEST DATA GENERATION COMPLETED!
```

## Notes

- Generated files (`test-input.py`, `test-output.json`, `test-data-*.json`) are in `.gitignore`
- Data files (`data/*.json`) are tracked in git for consistency
- SMS format matches actual Indian bank patterns (tested with SMS parser)
- All amounts stored in paise (e.g., ₹100.50 = 10050 paise)
- Transactions include proper SMS metadata fields (NULL for manual entries)

## Integration with Spendly

The script creates transactions compatible with Spendly's:
- Database schema version 4
- 19 unified categories (no expense/income distinction)
- 3 default accounts (My Account, Credit Card, Debit Card)
- SMS auto-detection confidence threshold (0.7)
- Indian bank SMS formats (HDFC, ICICI, SBI, UPI, etc.)
