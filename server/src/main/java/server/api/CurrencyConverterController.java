package server.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import server.services.CurrencyConverterService;

import java.time.Instant;

@Controller
@RequestMapping("/api/convert")
public class CurrencyConverterController {
    private final CurrencyConverterService converter;

    @Autowired
    public CurrencyConverterController(CurrencyConverterService converter) {
        this.converter = converter;
    }

    @PostMapping(path = {"", "/"})
    public ResponseEntity<Double> convert(@RequestBody String contents) {
        JsonObject body = JsonParser.parseString(contents).getAsJsonObject();
        System.out.println(body);
        double ret = converter.convert(Double.parseDouble(body.get("amount").toString()),
                body.get("from").toString().toString().substring(1, body.get("from").toString().length() - 1),
                body.get("to").toString().toString().substring(1, body.get("to").toString().length() - 1),
                Instant.parse(body.get("when").toString().substring(1, body.get("when").toString().length() - 1)));
        return ResponseEntity.ok(ret);
    }
}
