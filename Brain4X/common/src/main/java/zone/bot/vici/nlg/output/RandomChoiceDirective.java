package zone.bot.vici.nlg.output;

import freemarker.core.Environment;
import freemarker.template.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

class RandomChoiceDirective implements TemplateDirectiveModel {

    public void execute(final Environment env,
						final Map params, final TemplateModel[] loopVars,
						final TemplateDirectiveBody body)
            throws TemplateException, IOException {

        if(params.entrySet().iterator().hasNext()) {
            throw new TemplateModelException("This directive does not accept any parameter");
        }

        final StringWriter stringWriter = new StringWriter();
        body.render(stringWriter);
        final String rawContent = stringWriter.toString();
        final String[] unfilteredOptions = rawContent.split("\\|");
        if(unfilteredOptions.length<2) {
            throw new TemplateModelException("There must be at least 2 options available seperated by '|'");
        }
        final List<String> options = Arrays.stream(unfilteredOptions).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
        if(options.isEmpty()) {
            throw new TemplateModelException("There are only options with empty content available");
        }
        final int choice = new Random().nextInt(options.size());
        env.getOut().write(options.get(choice));
    }

}
