package com.byte_stefan.collect_util.util

import com.intellij.lang.java.JavaCommenter

class ExtJavaCommenter: JavaCommenter() {

    override fun getLineCommentPrefix(): String? {
        return "///"
    }
}