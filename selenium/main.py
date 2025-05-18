import unittest
import sys

if __name__ == "__main__":
    loader = unittest.TestLoader()

    if len(sys.argv) > 1:
        test_name = sys.argv[1]
        suite = loader.loadTestsFromName(f'tests.{test_name}')
        sys.argv = sys.argv[:1]
    else:
        suite = loader.discover('tests')

    runner = unittest.TextTestRunner()
    runner.run(suite)