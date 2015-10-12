package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.wav.WavOptions;
import org.jaudiotagger.audio.wav.WavSaveOptions;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.wav.WavTag;

import java.io.File;

/**
 * Test
 */
public class Issue084Test extends AbstractTestCase
{
    /**
     * Read file with metadata added by MediaMonkey
     */
    public void testSyncToId3HasInfoOnly()
    {

        Exception exceptionCaught = null;
        try
        {
            {
                TagOptionSingleton.getInstance().setWavOptions(WavOptions.READ_ID3_ONLY);
                TagOptionSingleton.getInstance().setWavSaveOptions(WavSaveOptions.SAVE_EXISTING_AND_ACTIVE);
                File testFile = AbstractTestCase.copyAudioToTmp("test123.wav", new File("test123Synced.wav"));
                AudioFile f = AudioFileIO.read(testFile);
                Tag tag = f.getTag();
                assertEquals("artistName\0", ((WavTag)tag).getInfoTag().getFirst(FieldKey.ARTIST));
                assertEquals("albumName\0", ((WavTag)tag).getInfoTag().getFirst(FieldKey.ALBUM));
                assertEquals("", tag.getFirst(FieldKey.ARTIST));
                assertEquals("", tag.getFirst(FieldKey.ALBUM));
                ((WavTag)tag).syncToId3FromInfo();
                assertEquals("artistName", tag.getFirst(FieldKey.ARTIST));
                assertEquals("albumName", tag.getFirst(FieldKey.ALBUM));
                f.commit();
                f = AudioFileIO.read(testFile);
                tag = f.getTag();
                assertEquals("artistName\0", ((WavTag)tag).getInfoTag().getFirst(FieldKey.ARTIST));
                assertTrue(((WavTag)tag).isExistingInfoTag());
                assertTrue(((WavTag)tag).isExistingId3Tag());
                assertEquals("artistName", ((WavTag)tag).getID3Tag().getFirst(FieldKey.ARTIST));
                assertEquals("artistName", tag.getFirst(FieldKey.ARTIST));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            exceptionCaught = e;
        }
        assertNull(exceptionCaught);
    }

    public void testSyncToInfoHasId3Only()
    {

        Exception exceptionCaught = null;
        try
        {
            {
                TagOptionSingleton.getInstance().setWavOptions(WavOptions.READ_INFO_ONLY);
                TagOptionSingleton.getInstance().setWavSaveOptions(WavSaveOptions.SAVE_BOTH);
                File testFile = AbstractTestCase.copyAudioToTmp("test126.wav", new File("test126Synced.wav"));
                AudioFile f = AudioFileIO.read(testFile);
                Tag tag = f.getTag();
                assertEquals("fred", ((WavTag)tag).getID3Tag().getFirst(FieldKey.ARTIST));
                assertEquals("", ((WavTag)tag).getInfoTag().getFirst(FieldKey.ARTIST));
                tag.setField(FieldKey.ARTIST, "fred");
                assertEquals("fred", tag.getFirst(FieldKey.ARTIST));
                ((WavTag)tag).syncToInfoFromId3();
                assertEquals("fred", tag.getFirst(FieldKey.ARTIST));
                f.commit();
                f = AudioFileIO.read(testFile);
                tag = f.getTag();
                assertEquals("fred", ((WavTag)tag).getID3Tag().getFirst(FieldKey.ARTIST));
                assertTrue(((WavTag)tag).isExistingInfoTag());
                assertTrue(((WavTag)tag).isExistingId3Tag());
                assertEquals("fred", ((WavTag)tag).getInfoTag().getFirst(FieldKey.ARTIST));
                assertEquals("fred", tag.getFirst(FieldKey.ARTIST));

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            exceptionCaught = e;
        }
        assertNull(exceptionCaught);
    }

}