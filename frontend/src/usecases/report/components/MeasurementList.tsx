import {normalize, schema} from 'normalizr';
import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {TimestampInfoMessage} from '../../../components/timestamp-info-message/TimestampInfoMessage';
import {timestamp} from '../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../helpers/formatters';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {
  MeasurementApiResponse,
  MeasurementResponsePart,
  Measurements,
} from '../../../state/ui/graph/measurement/measurementModels';
import {uuid} from '../../../types/Types';
import {LegendItem} from '../reportModels';

interface MeasurementListItem {
  id: uuid;
  label: string;
  city: string;
  address: string;
  unit: string;
  value: number | undefined;
  created: number;
}

const renderName = (item: MeasurementListItem) => orUnknown(item.label);
const renderCity = (item: MeasurementListItem) => orUnknown(item.city);
const renderAddress = (item: MeasurementListItem) => orUnknown(item.address);
const renderValue = ({value, unit}: MeasurementListItem): string =>
  value !== undefined && unit ? `${roundMeasurement(value)} ${unit}` : '-';
const renderCreated = (item: MeasurementListItem) => timestamp(item.created * 1000);

const lineSchema = [new schema.Entity('items', {}, {idAttribute: 'id'})];

const getMeasurementItems = (measurements: MeasurementApiResponse): MeasurementListItem[] => {
  const items: Map<uuid, MeasurementListItem> = new Map<uuid, MeasurementListItem>();

  measurements.forEach(({id, label, city, address, unit, values}: MeasurementResponsePart) =>
    values.forEach(({when, value}) => {
      const item: MeasurementListItem = {
        id: id + ';' + when,
        label,
        city,
        address,
        unit,
        value,
        created: when,
      };
      items.set(item.id, item);
    }));
  return Array.from(items.values());
};

export const MeasurementList = ({measurements}: Measurements) => {
  const {result, entities: {items}}: Normalized<LegendItem> = normalize(getMeasurementItems(measurements), lineSchema);

  return (
    <Column>
      <Table result={result} entities={items}>
        <TableColumn
          header={<TableHead className="first">{translate('facility')}</TableHead>}
          cellClassName={'first first-uppercase'}
          renderCell={renderName}
        />
        <TableColumn
          header={<TableHead>{translate('city')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderCity}
        />
        <TableColumn
          header={<TableHead>{translate('address')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderAddress}
        />
        <TableColumn
          header={<TableHead>{translate('value')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderValue}
        />
        <TableColumn
          header={<TableHead>{translate('readout')}</TableHead>}
          cellClassName={'first-uppercase'}
          renderCell={renderCreated}
        />
      </Table>
      <TimestampInfoMessage/>
    </Column>
  );
};
