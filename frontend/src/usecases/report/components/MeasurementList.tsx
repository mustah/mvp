import {DataResult, process, State} from '@progress/kendo-data-query';
import {ExcelExport} from '@progress/kendo-react-excel-export';
import {Grid, GridColumn} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {timestamp} from '../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../helpers/formatters';
import {firstUpperTranslated, translate} from '../../../services/translationService';
import {
  getMediumType,
  MeasurementApiResponse,
  MeasurementResponsePart,
  Measurements,
  Medium,
  Quantity,
} from '../../../state/ui/graph/measurement/measurementModels';
import {Callback, Dictionary} from '../../../types/Types';
import {cellRender, headerCellRender, rowRender} from '../helpers/measurementGridHelper';

export interface MeasurementListProps extends Measurements {
  isExportingToExcel: boolean;
  exportToExcelSuccess: Callback;
}

interface MeasurementListItem {
  when: string;
  label: string;
  values: { [key in Quantity]: string };
  medium: Medium;
}

const renderColumns = (measurements: MeasurementApiResponse): [MeasurementListItem[], React.ReactNode[]] => {
  const rows: Dictionary<MeasurementListItem> = {};
  const columns: Dictionary<React.ReactNode> = {};

  measurements.forEach(({id, label, medium, unit, values, quantity}: MeasurementResponsePart) => {
    if (columns[quantity] === undefined) {
      const key = `${translate(`${quantity} short`)} (${unit})`;
      columns[quantity] = (
        <GridColumn
          key={key}
          title={key}
          field={`values.${quantity}`}
        />
      );
    }

    values.forEach(({when, value}) => {
      const rowIndex = when + id;
      const listItem: MeasurementListItem = rows[rowIndex] || {
        label,
        medium: getMediumType(medium),
        when: timestamp(when * 1000),
        values: {},
      };
      listItem.values[quantity] = value !== undefined ? roundMeasurement(value) : '-';
      rows[rowIndex] = listItem;
    });
  });

  return [toArray(rows).reverse(), toArray(columns)];
};

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

  const state: State = {group: [{field: 'label'}]};
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
