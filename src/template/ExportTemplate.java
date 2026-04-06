package template;

import model.report.Report;

public abstract class ExportTemplate {

    // Template method — defines the algorithm skeleton; subclasses fill in the steps
    public final void export(Report report, String filePath) {
        prepareHeader(report);
        writeBody(report);
        formatFooter(report);
        save(report, filePath);
    }

    protected abstract void prepareHeader(Report report);
    protected abstract void writeBody(Report report);
    protected abstract void formatFooter(Report report);
    protected abstract void save(Report report, String filePath);
}
