import {DataResult, process, State} from '@progress/kendo-data-query';
import {ExcelExport} from '@progress/kendo-react-excel-export';
import {Grid, GridColumn} from '@progress/kendo-react-grid';
import {first} from 'lodash';
import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {isDefined} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {useExportToExcel} from '../../../hooks/exportToExcelHook';
import {firstUpperTranslated} from '../../../services/translationService';
import {MeasurementsApiResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {Callback} from '../../../types/Types';
import {cellRender, headerCellRender, renderColumns, rowRender} from '../helpers/measurementGridHelper';
import WorkbookSheet = kendo.ooxml.WorkbookSheet;
import WorkbookSheetRow = kendo.ooxml.WorkbookSheetRow;
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
  cell.format = 'yyyy-mm-dd hh:mm';
  cell.value = new Date(cell.value!.toString());
};

const indexType = 3;

const formatCell = (cell: WorkbookSheetRowCell, index: number) => {
  if (index > indexType) {
    formatReadoutValueCell(cell);
  } else if (index === indexType) {
    formatDateCell(cell);
  }
};

const skipHeaderRow = (_, index) => index > 0;

const save = (exporter: React.Ref<{}>) => {
  // TODO[!must!]: Our types for React's hooks are wrong. It is solved in the newest version of react.
  const component = ((exporter as any).current as ExcelExport);
  const options = component.workbookOptions();

  Maybe.maybe<WorkbookSheet>(first(options.sheets))
    .flatMap(sheet => Maybe.maybe<WorkbookSheetRow[]>(sheet.rows))
    .filter(isDefined)
    .do(rows => rows.filter(skipHeaderRow)
      .map(row => row.cells)
      .filter(isDefined)
      .forEach(cells => cells!.forEach(formatCell)));

  component.save(options);
};

const state: State = {group: [{field: 'label', dir: 'desc'}], sort: [{field: 'label', dir: 'asc'}]};

export const MeasurementList = ({measurements, exportToExcelSuccess, isExportingToExcel}: MeasurementListProps) => {
  const [listItems, quantityColumns] = React.useMemo(() => renderColumns(measurements), [measurements]);

  const exporter = useExportToExcel({isExportingToExcel, exportToExcelSuccess, save});

  const gridContent: React.ReactNode[] = [
    (
      <GridColumn
        headerClassName="hidden"
        className="hidden"
        field="name"
        key="name"
        title={firstUpperTranslated('name')}
      />
    ),
    (
      <GridColumn
        headerClassName="hidden"
        className="hidden"
        field="meterId"
        key="meterId"
        title={firstUpperTranslated('meter id')}
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
