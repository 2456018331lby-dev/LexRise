import tempfile
import unittest
from pathlib import Path

import build_wordlists


class MnemonicLoadingTest(unittest.TestCase):
    def write_csv(self, directory: Path, name: str, content: str) -> Path:
        path = directory / name
        path.write_text(content.strip() + "\n", encoding="utf-8")
        return path

    def test_load_mnemonics_allows_raw_file_to_override_seed(self):
        with tempfile.TemporaryDirectory() as tmp:
            root = Path(tmp)
            seed = self.write_csv(
                root,
                "seed.csv",
                """
                term,mnemonic
                describe,seed note
                export,move out
                """,
            )
            raw = self.write_csv(
                root,
                "raw.csv",
                """
                term,mnemonic
                describe,raw override
                """,
            )

            mnemonics = build_wordlists.load_mnemonics([seed, raw])

            self.assertEqual(mnemonics["describe"], "raw override")
            self.assertEqual(mnemonics["export"], "move out")

    def test_apply_mnemonics_matches_terms_case_insensitively(self):
        words = [{"term": "Describe"}, {"term": "unknown"}]

        count = build_wordlists.apply_mnemonics(words, {"describe": "write what you see"})

        self.assertEqual(count, 1)
        self.assertEqual(words[0]["mnemonic"], "write what you see")
        self.assertEqual(words[1]["mnemonic"], "")

    def test_load_mnemonics_rejects_missing_required_columns(self):
        with tempfile.TemporaryDirectory() as tmp:
            invalid = self.write_csv(
                Path(tmp),
                "invalid.csv",
                """
                term,note_text
                describe,missing mnemonic column
                """,
            )

            with self.assertRaisesRegex(ValueError, "term,mnemonic"):
                build_wordlists.load_mnemonics([invalid])


if __name__ == "__main__":
    unittest.main()
