import {ExcelExport} from '@progress/kendo-react-excel-export';
import {Grid, GridColumn} from '@progress/kendo-react-grid';
import {toArray} from 'lodash';
import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {timestamp} from '../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../helpers/formatters';
import {orUnknown} from '../../../helpers/translations';
import {firstUpperTranslated} from '../../../services/translationService';
import {
  MeasurementApiResponse,
  MeasurementResponsePart,
  Measurements,
  Quantity,
} from '../../../state/ui/graph/measurement/measurementModels';
import {Callback} from '../../../types/Types';

export interface MeasurementListProps extends Measurements {
  isExportingToExcel: boolean;
  exportToExcelSuccess: Callback;
}

interface MeasurementListItem {
  when: string;
  label: string;
  values: { [key in Quantity]: string };
}

const renderLabel = ({dataItem: {label}}) => <td>{orUnknown(label)}</td>;

const getMeasurementItems = (measurements: MeasurementApiResponse) => {
  const rows: {[key: string]: MeasurementListItem} = {};
  const columns = {};

  measurements.forEach(({id, label, unit, values, quantity}: MeasurementResponsePart) => {
    if (!columns[quantity]) {
      const key = `${quantity}: ${unit}`;
      columns[quantity] = (
        <GridColumn
          key={key}
          title={key}
          field={`values.${quantity}`}
        />
      );
    }

    values.forEach(({when, value}) => {
      const row: MeasurementListItem = rows[when + id] || {
        label,
        when: timestamp(when * 1000),
        values: {},
      };
      row.values[quantity] = value !== undefined ? roundMeasurement(value) : '-';
      rows[when + id] = row;
    });
  });

  return [toArray(rows).reverse(), toArray(columns)];
};

export const MeasurementList = ({measurements, exportToExcelSuccess, isExportingToExcel}: MeasurementListProps) => {
  const [data, quantityColumns] = React.useMemo(() => getMeasurementItems(measurements), [measurements]);

  const exporter = React.useRef();

  React.useEffect(() => {
    if (isExportingToExcel) {
      // TODO our types for React's hooks are wrong. Should be solved when upgrading react + types
      (exporter as any).current.save();
      exportToExcelSuccess();
    }
  }, [isExportingToExcel]);

  const columnsAsChildren = [
    (
      <GridColumn
        headerClassName="left-most"
        className="left-most"
        field="when"
        key="when"
        title={firstUpperTranslated('readout')}
      />
    ),
    (
      <GridColumn
        title={firstUpperTranslated('facility')}
        field="label"
        key="label"
        cell={renderLabel}
      />
    ),
    ...quantityColumns,
  ];

  return (
    <Column>
      <ExcelExport data={data} ref={exporter}>
        <Grid scrollable="none" data={data}>
          {columnsAsChildren}
        </Grid>
      </ExcelExport>
      <TimestampInfoMessage/>
    </Column>
  );
};
