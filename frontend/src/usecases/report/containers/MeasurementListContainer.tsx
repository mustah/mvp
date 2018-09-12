import {normalize, schema} from 'normalizr';
import * as React from 'react';
import {Row} from '../../../components/layouts/row/Row';
import {Table, TableColumn} from '../../../components/table/Table';
import {TableHead} from '../../../components/table/TableHead';
import {timestamp} from '../../../helpers/dateHelpers';
import {roundMeasurement} from '../../../helpers/formatters';
import {orUnknown} from '../../../helpers/translations';
import {translate} from '../../../services/translationService';
import {Normalized} from '../../../state/domain-models/domainModels';
import {
  MeasurementApiResponse,
  MeasurementApiResponsePart,
} from '../../../state/ui/graph/measurement/measurementModels';
import {uuid} from '../../../types/Types';
import {LegendItem} from '../reportModels';
import './MeasurementListContainer.scss';

interface OwnProps {
  measurement: MeasurementApiResponse;
}

interface MeasurementListItem {
  id: uuid;
  label: string;
  city: string;
  address: string;
  unit: string;
  value: number | undefined;
  created: number;
}

type Props = OwnProps;

const renderName = (item: MeasurementListItem) => orUnknown(item.label);
const renderCity = (item: MeasurementListItem) => orUnknown(item.city);
const renderAddress = (item: MeasurementListItem) => orUnknown(item.address);
const renderValue = ({value = null, unit}: MeasurementListItem): string =>
  value !== null && unit ? `${roundMeasurement(value)} ${unit}` : '';
const renderCreated = (item: MeasurementListItem) => timestamp(item.created * 1000);

export const MeasurementListContainer = ({measurement}: Props) => {
  const items: Map<uuid, MeasurementListItem> = new Map<uuid, MeasurementListItem>();

  measurement.forEach(({id, label, city, address, unit, values}: MeasurementApiResponsePart) => {
    values.forEach(({when, value}) => {
      const item: MeasurementListItem = {
        id: id + ';' + when, label, city, address, unit, value, created: when,
      };
      items.set(item.id, item);
    });
  });

  const lineSchema = [new schema.Entity('items', {}, {idAttribute: 'id'})];
  const normalized: Normalized<LegendItem> = normalize(Array.from(items.values()), lineSchema);

  return (
    <Row className="measurement-list">
      <Table result={normalized.result} entities={normalized.entities.items}>
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
    </Row>
  );
};
