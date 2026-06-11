package com.gu.recipe.loader

import com.gu.recipe.noCustomaryTerminologySession
import com.gu.recipe.terminology.TerminologyConverter
import com.gu.recipe.terminology.setUpTerminologyTable


class TerminologyLoader(
    bridge: TerminologyLoaderBridge,
    onError: ((String) -> Unit)? = null
) : BaseLoader<TerminologyConverter>(bridge, onError) {
    override fun loadTable(data: String?): Result<TerminologyConverter> {
        return setUpTerminologyTable(data)
    }

    override fun fallbackSession(): TerminologyConverter {
        return setUpTerminologyTable(null).getOrElse {
            onError?.invoke("Internal data also failed: ${it.message}")
            noCustomaryTerminologySession()
        }
    }
}