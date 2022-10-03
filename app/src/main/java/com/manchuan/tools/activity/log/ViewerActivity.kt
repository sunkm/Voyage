package com.manchuan.tools.activity.log

import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.dylanc.longan.safeIntentExtras
import com.gyf.immersionbar.ktx.immersionBar
import com.manchuan.tools.databinding.ActivityViewerBinding
import com.manchuan.tools.extensions.colorPrimary
import com.manchuan.tools.extensions.windowBackground
import com.manchuan.tools.utils.UiUtils
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.text.LineSeparator
import io.github.rosemoe.sora.widget.CodeEditor
import io.github.rosemoe.sora.widget.style.builtin.ScaleCursorAnimator
import org.eclipse.tm4e.core.registry.IGrammarSource
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.File
import java.io.InputStreamReader

class ViewerActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityViewerBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        immersionBar {
            titleBar(binding.toolbar)
            transparentStatusBar()
            statusBarDarkFont(!UiUtils.isDarkMode())
            navigationBarColorInt(windowBackground())
        }
        binding.toolbar.apply {
            title = "日志预览"
            setTitleTextColor(colorPrimary())
            setNavigationOnClickListener {
                finish()
            }
        }
        val typeface = Typeface.createFromAsset(assets, "JetBrainsMono-Regular.ttf")
        binding.editor.apply {
            lineSeparator = LineSeparator.CRLF
            typefaceText = typeface
            setLineSpacing(2f, 1.1f)
            cursorAnimator = ScaleCursorAnimator(this)
            nonPrintablePaintingFlags =
                CodeEditor.FLAG_DRAW_WHITESPACE_LEADING or CodeEditor.FLAG_DRAW_LINE_SEPARATOR or CodeEditor.FLAG_DRAW_WHITESPACE_IN_SELECTION
        }
        ensureTextmateTheme()
        val themeSource =
            IThemeSource.fromInputStream(assets.open("textmate/darcula.json"), "darcula.json", null)
        val editorColorScheme = TextMateColorScheme.create(themeSource)
        binding.editor.colorScheme = editorColorScheme
        val language =
            TextMateLanguage.create(IGrammarSource.fromInputStream(assets.open("textmate/java/syntaxes/java.tmLanguage.json"),
                "java.tmLanguage.json",
                null),
                InputStreamReader(assets.open("textmate/java/language-configuration.json")),
                (binding.editor.colorScheme as TextMateColorScheme).themeSource)
        binding.editor.setEditorLanguage(language)
        binding.editor.setText(File(safeIntentExtras<String>("file").value).readText())
    }

    private fun ensureTextmateTheme() {
        val editor = binding.editor
        var editorColorScheme = editor.colorScheme
        if (editorColorScheme !is TextMateColorScheme) {
            val themeSource = IThemeSource.fromInputStream(
                assets.open("textmate/QuietLight.tmTheme"),
                "QuietLight.tmTheme",
                null
            )
            editorColorScheme = TextMateColorScheme.create(themeSource)
            editor.colorScheme = editorColorScheme
        }
    }

}