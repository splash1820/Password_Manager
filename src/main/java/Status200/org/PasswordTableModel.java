package Status200.org;

// PasswordTableModel.java
import javax.swing.table.AbstractTableModel;
import java.util.List;

public class PasswordTableModel extends AbstractTableModel {
    private final String[] cols = {"Description", "Username", "Password", "Strength"};
    private List<PasswordEntry> data;

    public PasswordTableModel(List<PasswordEntry> data) {
        this.data = data;
    }

    public void setData(List<PasswordEntry> data) {
        this.data = data;
        fireTableDataChanged();
    }

    public PasswordEntry getAt(int row) {
        return data.get(row);
    }

    @Override
    public int getRowCount() { return data.size(); }
    @Override
    public int getColumnCount() { return cols.length; }
    @Override
    public String getColumnName(int column) { return cols[column]; }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        PasswordEntry e = data.get(rowIndex);
        switch (columnIndex) {
            case 0: return e.getDescription();
            case 1: return e.getUsername();
            case 2: return e.getPassword(); // renderer will mask if needed
            case 3: return e.getStrength().name();
            default: return "";
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        // allow inline editing for description, username, password (columns 0..2)
        return columnIndex >= 0 && columnIndex <= 2;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        PasswordEntry e = data.get(rowIndex);
        if (aValue == null) return;
        switch (columnIndex) {
            case 0: e.setDescription(aValue.toString()); break;
            case 1: e.setUsername(aValue.toString()); break;
            case 2:
                e.setPassword(aValue.toString());
                e.setStrength(Services.estimateStrength(aValue.toString()));
                break;
        }
        Repository.getInstance().update(e);
        fireTableRowsUpdated(rowIndex, rowIndex);
    }


}

