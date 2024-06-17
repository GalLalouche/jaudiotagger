package org.jaudiotagger.issues;

import org.jaudiotagger.AbstractTestCase;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.wav.WavOptions;
import org.jaudiotagger.audio.wav.WavSaveOptions;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.TagOptionSingleton;
import org.jaudiotagger.tag.reference.ID3V2Version;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Test, useful for just reading file info when actually encoded as Japanese
 */
public class Issue657Test extends AbstractTestCase
{
    /**
     * In this file the tags in the Riff are encoded as CP932, so we set overide charset to read correctly
     * and then write to new ID3 tag using encoding UTF-16 because that can handle Japanese charsets
     *
     * @throws Exception
     */
    public void testFixBadWavListInfoEncoding() throws Exception
    {
        final TagOptionSingleton tagOptions = TagOptionSingleton.getInstance();
        tagOptions.setToDefault();
        tagOptions.setWavOptions(WavOptions.READ_ID3_ONLY_AND_SYNC);
        tagOptions.setWavSaveOptions(WavSaveOptions.SAVE_ACTIVE);
        tagOptions.setOverrideCharsetForInfo(true);
        tagOptions.setOverrideCharsetForId3(false);
        tagOptions.setOverrideCharset(Charset.forName("x-MS932_0213"));
        tagOptions.setResetTextEncodingForExistingFrames(true);
        tagOptions.setID3V2Version(ID3V2Version.ID3_V23);
        tagOptions.addOverrideCharsetFields(FieldKey.ARTIST);

        File orig = new File("testdata", "test659.wav");
        if (!orig.isFile())
        {
            System.err.println("Unable to test file - not available");
            return;
        }

        Exception ex=null;
        try
        {
            //Read using the encoding
            File testFile = AbstractTestCase.copyAudioToTmp("test659.wav");
            AudioFile af = AudioFileIO.read(testFile);
            assertNotNull(af.getTag());
            System.out.println(af.getTag());

            //Reads artist correctly because specified as an aoveride field
            assertEquals(af.getTag().getFirst(FieldKey.ARTIST), "カルロス・モントーヤ");

            //but not album because not specified as a field
            assertEquals(af.getTag().getFirst(FieldKey.ALBUM), "�x�X�g�E�I�u�E�t�������R�E�M�^�[");

            //So add other fields and retry
            tagOptions.addOverrideCharsetFields(FieldKey.GENRE);
            tagOptions.addOverrideCharsetFields(FieldKey.ALBUM);
            tagOptions.addOverrideCharsetFields(FieldKey.TITLE);

            af = AudioFileIO.read(testFile);
            assertNotNull(af.getTag());
            System.out.println(af.getTag());

            assertEquals(af.getTag().getFirst(FieldKey.ARTIST), "カルロス・モントーヤ");
            assertEquals(af.getTag().getFirst(FieldKey.ALBUM), "ベスト・オブ・フラメンコ・ギター");

            //Then save back so should write correctly to id3
            af.getTagOrCreateAndSetDefault().setField(FieldKey.ARTIST,"カルロス");
            af.commit();

            //Read again, no longer set charset because now should be written to ID3 tag okay
            tagOptions.setOverrideCharset(null);
            af = AudioFileIO.read(testFile);
            assertNotNull(af.getTag());
            assertEquals(af.getTag().getFirst(FieldKey.ARTIST), "カルロス");
            assertEquals(af.getTag().getFirst(FieldKey.ALBUM), "ベスト・オブ・フラメンコ・ギター");
            System.out.println(af.getTag());
        }
        catch(Exception e)
        {
            e.printStackTrace();
            ex=e;
        }
        assertNull(ex);
        tagOptions.setOverrideCharset(null);
    }
}
