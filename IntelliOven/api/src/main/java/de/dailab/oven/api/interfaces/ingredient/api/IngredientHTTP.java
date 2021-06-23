package de.dailab.oven.api.interfaces.ingredient.api;


import de.dailab.oven.database.IngredientController;
import de.dailab.oven.database.exceptions.ConfigurationException;
import de.dailab.oven.database.exceptions.DatabaseException;
import de.dailab.oven.database.query.Query;
import de.dailab.oven.model.data_model.Unit;
import de.dailab.oven.model.util.UnitTranslationsUtil;
import de.dailab.oven.model.util.UnitTranslationsUtil.UnitTranslationData;
import org.apache.commons.codec.language.bm.Lang;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import zone.bot.vici.Language;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("oven/ingredient")
public class IngredientHTTP {

    @Nonnull
    private final IngredientController ingredientController;
    @Nonnull
    private final Map<String, Map<String, UnitTranslationData>> unitLabels = new HashMap<>();

    public IngredientHTTP() throws DatabaseException, ConfigurationException {
        this.ingredientController = new IngredientController(new Query().getGraph());
        for(final Language language : Language.getLanguages()) {
            final Map<String, UnitTranslationData> unitMap = new HashMap<>();
            this.unitLabels.put(language.getLangCode2(), unitMap);
            for(final Unit unit : Unit.values()) {
                unitMap.put(unit.name(), unit.getLabels(language));
            }
        }
    }

    /**
     * Get a list with all ingredient names (by language if specified).
     *
     * @return List of ingredients
     */
    @GetMapping(value = "/getAll")
    public ResponseEntity getAll(@RequestHeader(value = "language", defaultValue = "") final String languageCode) {
        final Language language = languageCode.isEmpty() ? null : Language.getLanguage(languageCode);
        return ResponseEntity.status(HttpStatus.OK).body(this.ingredientController.listIngredientNames(language));
    }

    @GetMapping(value = "/units")
    public ResponseEntity getUnitLabels() {
        return ResponseEntity.status(HttpStatus.OK).body(this.unitLabels);
    }

}
