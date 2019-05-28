package gov.nih.ncats.common.cli;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Created by katzelda on 6/21/17.
 */
public class RadioGroup implements OptionGroup {

    private boolean isRequired;

    private final List<Option> options;

    private final List<OptionGroup> optionGroups;

    public static RadioGroup required(CliOption...options){
        return new RadioGroup(true, Arrays.asList(options));
    }
    public static RadioGroup optional(CliOption...options){
        return new RadioGroup(false, Arrays.asList(options));
    }

    private RadioGroup(boolean isRequired, List<CliOption> options) {
        this.isRequired = isRequired;
        this.options = new ArrayList<>(options.size());
        this.optionGroups = new ArrayList<>(options.size());
        for(CliOption o : options){
            Objects.requireNonNull(o);
            if(o instanceof OptionGroup){

                OptionGroup o1 = (OptionGroup) o;
                o1.setIsRequired(false);
                optionGroups.add(o1);
            }else{
                this.options.add( (Option) o);
            }
        }
    }

    @Override
    public void setIsRequired(boolean b) {
        this.isRequired = b;
    }

    @Override
    public void visit(OptionVisitor visitor, Boolean forcedOverride) {
        visitor.preVisit(this);
        for(Option o : options){
            org.apache.commons.cli.Option opt = o.asApacheOption();
            //set not required for cli validation
            opt.setRequired(false);
            visitor.visit(opt, o.getConsumer());
        }
        for(OptionGroup og: optionGroups){
//            og.setIsRequired(false);
           og.visit(visitor, false);
        }
        visitor.postVisit(this);
    }

    public boolean isRequired() {
        return isRequired;
    }
}
