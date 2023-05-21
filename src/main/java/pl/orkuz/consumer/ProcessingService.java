package pl.orkuz.consumer;

import org.apache.kafka.common.utils.Bytes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;
import ws.schild.jave.encode.VideoAttributes;
import ws.schild.jave.encode.enums.X264_PROFILE;
import ws.schild.jave.info.VideoSize;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

@Service
public class ProcessingService {

    @Autowired
    private KafkaTemplate<String, Bytes> kafkaTemplate;

    @KafkaListener(id="id", topics = "videoStorage1")
    public void processVideo(Bytes kafkaBytes) throws IOException, EncoderException {
        byte[] fileBytes = kafkaBytes.get();
        File videoFile = new File("video.mp4");
        FileOutputStream fostream = new FileOutputStream(videoFile);
        fostream.write(fileBytes);
        fostream.flush();
        fostream.close();
        logToConsole("Przyjęto plik: " + videoFile.getName() + "Konwertowanie");
        File result = convertToAvi(videoFile);
        logToConsole("Konwersja skończona. Nowy plik: " + result.getName());
        sendBackToKafka(result);
        logToConsole("Plik wysłany do kafki.");

    }

    private void logToConsole(String message) {
        System.out.println("=======================================");
        System.out.println(message);
        System.out.println("=======================================");
    }

    private void sendBackToKafka(File convertedFile) throws IOException {
        kafkaTemplate.send("videoStorage2", new Bytes(Files.readAllBytes(convertedFile.toPath())));
    }

    private File convertToAvi(File source) throws EncoderException {
        File target = new File("result.avi");

        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("aac");
        audio.setBitRate(64000);
        audio.setChannels(2);
        audio.setSamplingRate(44100);

        VideoAttributes video = new VideoAttributes();
        video.setCodec("h264");
        video.setX264Profile(X264_PROFILE.BASELINE);
        video.setBitRate(160000);
        video.setFrameRate(15);
        video.setSize(new VideoSize(400, 300));

        EncodingAttributes attributes = new EncodingAttributes();
        attributes.setInputFormat("mp4");
        attributes.setOutputFormat("avi");

        attributes.setVideoAttributes(video);
        attributes.setAudioAttributes(audio);

        Encoder encoder = new Encoder();
        encoder.encode(new MultimediaObject(source), target, attributes);

        return target;
    }

}
