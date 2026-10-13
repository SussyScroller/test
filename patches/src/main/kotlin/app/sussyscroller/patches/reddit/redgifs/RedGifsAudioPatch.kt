/*
 * Copyright 2026 Morphe.
 * https://github.com/MorpheApp/morphe-patches
 *
 * See the included NOTICE file for GPLv3 Section 7 terms that apply to this code.
 *
 * Originally written by DarkGooner: https://github.com/DarkGooner/morphe-patches (v1.47.9).
 * Modified 2026-10-13 by SussyScroller for a standalone patch source: removed the settings
 * patch dependency and the settings toggle, and uses a self-contained extension.
 */

package app.sussyscroller.patches.reddit.redgifs

import app.sussyscroller.patches.shared.Constants.COMPATIBILITY_REDDIT
import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.addInstructionsWithLabels
import app.morphe.patcher.patch.bytecodePatch
import com.android.tools.smali.dexlib2.Opcode

private const val EXTENSION_CLASS =
    "Lapp/sussyscroller/redgifs/extension/RedGifsAudioPatch;"

@Suppress("unused")
val redGifsAudioPatch = bytecodePatch(
    name = "Enable RedGifs audio",
    description = "Plays RedGifs videos with sound, using the original video from the RedGifs API."
) {
    compatibleWith(COMPATIBILITY_REDDIT)

    // This patch has no settings, and does not depend on any patch of the official Morphe
    // patch source, so it can be used together with it.
    // The extension name is unique, because all patch sources share one class loader.
    extendWith("extensions/sussyscroller-redgifs.mpe")

    execute {

        // Start fetching the RedGifs video url as soon as a post is loaded,
        // so it's ready before the video is first played.
        LinkConstructorFingerprint.method.apply {
            val returnIndex = implementation!!.instructions.indexOfLast {
                it.opcode == Opcode.RETURN_VOID
            }

            // All registers are unused at the final return, so v0 and v1 can be overwritten.
            // Older RedGifs posts have no Reddit hosted video and open in the browser.
            // For these a preview video is set, and the post hint is changed to match newer
            // RedGifs posts, so Reddit plays them in the app.
            addInstructionsWithLabels(
                returnIndex,
                """
                    move-object/from16 v0, p0
                    invoke-static { v0 }, $EXTENSION_CLASS->onLinkCreated(Lcom/reddit/domain/model/Link;)Lcom/reddit/domain/model/Preview;
                    move-result-object v1
                    if-eqz v1, :keep_preview
                    iput-object v1, v0, Lcom/reddit/domain/model/Link;->preview:Lcom/reddit/domain/model/Preview;
                    const-string v1, "rich:video"
                    iput-object v1, v0, Lcom/reddit/domain/model/Link;->postHint:Ljava/lang/String;
                    :keep_preview
                    nop
                """
            )
        }

        // Every video player url, including feed videos that are created without the post.
        VideoUrlsConstructorFingerprint.method.addInstructions(
            0,
            """
                invoke-static { p1 }, $EXTENSION_CLASS->getPlaybackUrl(Ljava/lang/String;)Ljava/lang/String;
                move-result-object p1
            """
        )

        // Post detail and full screen videos. These replace the url before the video player
        // is created. Optional, because the VideoUrls hook also handles these videos, and these
        // obfuscated methods are the most likely to change between Reddit versions.
        LinkVideoUrlFingerprint.methodOrNull?.addInstructionsWithLabels(
            0,
            """
                move-object/from16 v0, p1
                invoke-virtual { v0 }, Lcom/reddit/domain/model/Link;->getUrl()Ljava/lang/String;
                move-result-object v0
                invoke-static { v0 }, $EXTENSION_CLASS->getVideoUrl(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v0
                if-eqz v0, :original
                return-object v0
                :original
                nop
            """
        )

        LinkMediaVideoUrlFingerprint.methodOrNull?.addInstructionsWithLabels(
            0,
            """
                move-object/from16 v0, p4
                invoke-static { v0 }, $EXTENSION_CLASS->getVideoUrl(Ljava/lang/String;)Ljava/lang/String;
                move-result-object v0
                if-eqz v0, :original
                return-object v0
                :original
                nop
            """
        )
    }
}
