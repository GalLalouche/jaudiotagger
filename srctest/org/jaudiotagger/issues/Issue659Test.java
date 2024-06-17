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

/**
 * Test, useful for just reading file info when actually encoded as Japanese
 */
public class Issue659Test extends AbstractTestCase
{
    /**
     * Fields in the ID3 tag are encoded as UTF-16, but encoding bit is set to zero indicating ISO-8859-1 so we
     * need to set override charset to force correct read
     *
     * @throws Exception
     */
    public void testFixBadWavId3v23Encoding() throws Exception
    {
        final TagOptionSingleton tagOptions = TagOptionSingleton.getInstance();
        tagOptions.setToDefault();
        tagOptions.setWavOptions(WavOptions.READ_ID3_ONLY_AND_SYNC);
        tagOptions.setWavSaveOptions(WavSaveOptions.SAVE_ACTIVE);
        tagOptions.setOverrideCharsetForInfo(false);
        tagOptions.setOverrideCharsetForId3(true);
        tagOptions.setOverrideCharset(Charset.forName("UTF-16"));
        tagOptions.setResetTextEncodingForExistingFrames(true);
        tagOptions.setID3V2Version(ID3V2Version.ID3_V23);
        tagOptions.addOverrideCharsetFields(FieldKey.ARTIST);

        File orig = new File("testdata", "test660.wav");
        if (!orig.isFile())
        {
            System.err.println("Unable to test file - not available");
            return;
        }

        Exception ex=null;
        try
        {
            //Read using the encoding
            File testFile = AbstractTestCase.copyAudioToTmp("test660.wav");
            AudioFile af = AudioFileIO.read(testFile);
            assertNotNull(af.getTag());
            System.out.println(af.getTag());
            String カルロス = "カルロス";
            assertEquals(af.getTag().getFirst(FieldKey.ARTIST), カルロス + "・モントーヤ");

            //Then save back so should write correctly to id3
            af.getTagOrCreateAndSetDefault().setField(FieldKey.ARTIST,"カルロス");
            af.commit();

            //Read again, no longer set charset because now should be written to ID3 tag okay
            tagOptions.setOverrideCharset(null);
            af = AudioFileIO.read(testFile);
            assertNotNull(af.getTag());
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
