package gov.nih.ncats.common.cli;

/**
 * Created by katzelda on 6/21/17.
 */
public interface CliOption {

    void visit(OptionVisitor visitor);
}
