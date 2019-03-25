import {DataResult, process, State} from '@progress/kendo-data-query';
import {ExcelExport} from '@progress/kendo-react-excel-export';
import {Grid, GridColumn} from '@progress/kendo-react-grid';
import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {useExportToExcel} from '../../../hooks/exportToExcelHook';
import {firstUpperTranslated} from '../../../services/translationService';
import {MeasurementsApiResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {Callback} from '../../../types/Types';
import {cellRender, headerCellRender, renderColumns, rowRender} from '../helpers/measurementGridHelper';
import WorkbookSheetRowCell = kendo.ooxml.WorkbookSheetRowCell;

export interface MeasurementListProps {
  measurements: MeasurementsApiResponse;
  isExportingToExcel: boolean;
  exportToExcelSuccess: Callback;
}

const formatReadoutValueCell = (cell: WorkbookSheetRowCell) => {
  cell.format = '0.000';
  cell.value = isNaN(Number(cell.value)) ? cell.value : Number(cell.value);
};

const formatDateCell = (cell: WorkbookSheetRowCell) => {
  cell.format = 'YYYY-MM-DD HH:MM:SS';
  cell.value = new Date(cell.value!.toString());
};

const formatCell = (cell: WorkbookSheetRowCell, index: number) => {
  if (index > 2) {
    formatReadoutValueCell(cell);
  } else if (index === 2) {
    formatDateCell(cell);
  }
};

const save = (exporter: React.Ref<{}>) => {
  // TODO[!must!]: Our types for React's hooks are wrong. It is solved in the newest version of react.
  const component = ((exporter as any).current as ExcelExport);
  const options = component.workbookOptions();

  const sheets = options.sheets;
  if (sheets !== undefined && sheets.length > 0) {
    const sheet = sheets[0];
    if (sheet.rows !== undefined) {
      sheet.rows
        .filter((row, index) => index > 0) // skip header row
        .forEach(row => row.cells !== undefined && row.cells.forEach(formatCell));
    }
  }
  component.save(options);
};

export const MeasurementList = ({measurements, exportToExcelSuccess, isExportingToExcel}: MeasurementListProps) => {
  const [listItems, quantityColumns] = React.useMemo(() => renderColumns(measurements), [measurements]);

  const exporter = useExportToExcel({isExportingToExcel, exportToExcelSuccess, save});

  const gridContent: React.ReactNode[] = [
    (
      <GridColumn
        headerClassName="hidden"
        className="hidden"
        field="label"
        key="label"
        title={firstUpperTranslated('name')}
      />
    ),
    (
      <GridColumn
        headerClassName="hidden"
        className="hidden"
        field="type"
        key="type"
        title={firstUpperTranslated('object type')}
      />
    ),
    (
      <GridColumn
        headerClassName="left-most"
        className="left-most"
        field="when"
        key="when"
        title={firstUpperTranslated('readout')}
      />
    ),
    ...quantityColumns,
  ];

  const state: State = {group: [{field: 'label', dir: 'desc'}], sort: [{field: 'label', dir: 'asc'}]};
  const dataResult: DataResult = process(listItems, state);
  return (
    <Column className="Grouping-grid">
      <ExcelExport data={listItems} ref={exporter} filterable={true}>
        <Grid
          scrollable="none"
          data={dataResult}
          groupable={true}
          cellRender={cellRender}
          headerCellRender={headerCellRender}
          rowRender={rowRender}
          {...state}
        >
          {gridContent}
        </Grid>
      </ExcelExport>
      <TimestampInfoMessage/>
    </Column>
  );
};
