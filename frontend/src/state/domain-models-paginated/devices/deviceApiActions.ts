import {normalize, schema, Schema} from 'normalizr';
import {EndPoints} from '../../../services/endPoints';
import {Sectors} from '../../../types/Types';
import {DataFormatter} from '../../domain-models/domainModelsActions';
import {updatePageMetaData} from '../../ui/pagination/paginationActions';
import {NormalizedPaginated} from '../paginatedDomainModels';
import {fetchIfNeeded} from '../paginatedDomainModelsActions';
import {Device} from './deviceModels';

export const processStrategy = ({deviceEui: id, ...rest}: any): schema.StrategyFunction<Device> =>
  ({...rest, id});

const content = [new schema.Entity('devices', {}, {idAttribute: 'deviceEui', processStrategy})];
const devicesSchema: Schema = {content};

const dataFormatter: DataFormatter<NormalizedPaginated<Device>> =
  (response) => normalize(response, devicesSchema) as NormalizedPaginated<Device>;

export const fetchDevices = fetchIfNeeded(
  Sectors.devices,
  EndPoints.devices,
  dataFormatter,
  'devices',
  {
    afterSuccess: (
      {result}: NormalizedPaginated<Device>,
      dispatch,
    ) => dispatch(updatePageMetaData({entityType: 'devices', ...result})),
  },
);
