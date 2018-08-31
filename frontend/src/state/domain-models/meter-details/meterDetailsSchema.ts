import {normalize, schema, Schema} from 'normalizr';
import {Normalized} from '../domainModels';
import {DataFormatter} from '../domainModelsActions';
import {User} from '../user/userModels';

export const meterDetailsProcessStrategy = (entity: any): schema.StrategyFunction => {
  if (entity.status) {
    const statusCode = entity.status.toLowerCase();
    const status = {id: statusCode, name: statusCode};
    const gatewayStatus = entity.gateway.status;
    return {
      ...entity,
      status,
      gateway: {...entity.gateway, status: {id: gatewayStatus, name: gatewayStatus}},
    };
  }
  return entity;
};
const meterDetails: Schema = new schema.Entity('meters', {}, {processStrategy: meterDetailsProcessStrategy});

export const meterDetailsDataFormatter: DataFormatter<Normalized<User>> =
  (response) => normalize(response, meterDetails);
