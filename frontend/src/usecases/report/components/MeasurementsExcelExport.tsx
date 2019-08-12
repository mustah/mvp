import {ExcelExport, ExcelExportColumn} from '@progress/kendo-react-excel-export';
import {first} from 'lodash';
import * as React from 'react';
import {withContent} from '../../../components/hoc/withContent';
import {isDefined} from '../../../helpers/commonHelpers';
import {Maybe} from '../../../helpers/Maybe';
import {useExportToExcel} from '../../../hooks/exportToExcelHook';
import {firstUpperTranslated} from '../../../services/translationService';
import {MeasurementState} from '../../../state/ui/graph/measurement/measurementModels';
import {Callback, EncodedUriParameters, HasContent} from '../../../types/Types';
import {renderColumns} from '../helpers/measurementGridHelper';
import WorkbookSheet = kendo.ooxml.WorkbookSheet;
import WorkbookSheetRow = kendo.ooxml.WorkbookSheetRow;
import WorkbookSheetRowCell = kendo.ooxml.WorkbookSheetRowCell;

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

const save = (exporter: React.RefObject<ExcelExport>): void => {
  const component = exporter.current!;
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

export interface StateToProps extends HasContent {
  measurement: MeasurementState;
  parameters: EncodedUriParameters;
}

export interface DispatchToProps {
  exportToExcelSuccess: Callback;
}

type Props = StateToProps & DispatchToProps;

const ExcelExportComponent = ({
  measurement: {measurementResponse: {measurements}, isExportingToExcel},
  exportToExcelSuccess,
}: Props) => {
  const [listItems, columns] = React.useMemo(() => renderColumns(measurements), [measurements]);

  const exporter = useExportToExcel({isExportingToExcel, exportToExcelSuccess, save});

  return (
    <>
      <ExcelExport data={listItems} ref={exporter} filterable={true} fileName="measurements.xlsx">
        <ExcelExportColumn field="name" title={firstUpperTranslated('name')}/>
        <ExcelExportColumn field="meterId" title={firstUpperTranslated('meter id')}/>
        <ExcelExportColumn field="type" title={firstUpperTranslated('object type')}/>
        <ExcelExportColumn field="when" title={firstUpperTranslated('readout')}/>
        {columns.map(({field, title}) => <ExcelExportColumn field={field} title={title} key={title}/>)}
      </ExcelExport>
    </>
  );
};

export const MeasurementsExcelExport = withContent(ExcelExportComponent);
