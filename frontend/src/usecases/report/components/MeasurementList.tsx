import {DataResult, process, State} from '@progress/kendo-data-query';
import {ExcelExport} from '@progress/kendo-react-excel-export';
import {Grid, GridColumn} from '@progress/kendo-react-grid';
import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {firstUpperTranslated} from '../../../services/translationService';
import {MeasurementsApiResponse} from '../../../state/ui/graph/measurement/measurementModels';
import {Callback} from '../../../types/Types';
import {cellRender, headerCellRender, renderColumns, rowRender} from '../helpers/measurementGridHelper';

export interface MeasurementListProps {
  measurements: MeasurementsApiResponse;
  isExportingToExcel: boolean;
  exportToExcelSuccess: Callback;
}

export const MeasurementList = ({measurements, exportToExcelSuccess, isExportingToExcel}: MeasurementListProps) => {
  const [listItems, quantityColumns] = React.useMemo(() => renderColumns(measurements), [measurements]);

  const exporter = React.useRef();

  React.useEffect(() => {
    if (isExportingToExcel) {
      // TODO[!must!]: Our types for React's hooks are wrong. It is solved in the newest version of react.
      (exporter as any).current.save();
      exportToExcelSuccess();
    }
  }, [isExportingToExcel]);

  const gridContent: React.ReactNode[] = [
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
      <ExcelExport data={dataResult.data} ref={exporter} {...state}>
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
