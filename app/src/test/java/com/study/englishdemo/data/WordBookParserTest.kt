package com.study.englishdemo.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class WordBookParserTest {
    private val parser = WordBookParser()

    @Test
    fun parseCsv_readsStructuredRows() {
        val csv = """
            term,phonetic,definition,translation,example,tags
            clarify,/ˈklærəfaɪ/,make easy to understand,澄清,"Please clarify the goal.",cet4|core
        """.trimIndent().byteInputStream()

        val preview = parser.parseCsv(csv, "CSV 词书")

        assertThat(preview.words).hasSize(1)
        assertThat(preview.words.first().term).isEqualTo("clarify")
        assertThat(preview.words.first().tags).containsExactly("cet4", "core")
    }

    @Test
    fun parseCsv_readsMnemonicWhenPresentInExtendedRows() {
        val csv = """
            term,phonetic,definition,translation,example,tags,rootKey,derivatives,frq,pos,mnemonic
            describe,/dɪˈskraɪb/,write down,描述,Describe the picture.,cet4|scrib,scrib,described|describes,1200,v.,de + scrib：把看到的写下来就是 describe 描述
        """.trimIndent().byteInputStream()

        val preview = parser.parseCsv(csv, "CSV 词书")

        assertThat(preview.words.first().mnemonic)
            .isEqualTo("de + scrib：把看到的写下来就是 describe 描述")
    }

    @Test
    fun parseText_supportsSimpleSeparator() {
        val txt = """
            abandon :: 放弃 :: Never abandon your plan.
            benefit :: 益处 :: Daily practice brings benefit.
        """.trimIndent().byteInputStream()

        val preview = parser.parseText(txt, "TXT 词书")

        assertThat(preview.words).hasSize(2)
        assertThat(preview.words.first().translation).isEqualTo("放弃")
        assertThat(preview.words.first().example).contains("plan")
    }

    @Test
    fun parseText_rejectsInvalidContent() {
        val txt = """
            no separator here
            still invalid
        """.trimIndent().byteInputStream()

        val error = runCatching { parser.parseText(txt, "TXT 词书") }.exceptionOrNull()

        assertThat(error).isNotNull()
        assertThat(error).hasMessageThat().contains("未解析到有效词条")
    }
}
