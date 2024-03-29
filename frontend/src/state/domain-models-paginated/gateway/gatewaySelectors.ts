import {createSelector} from 'reselect';
import {Maybe} from '../../../helpers/Maybe';
import {uuid} from '../../../types/Types';
import {Gateway} from './gatewayModels';

export const getGatewayMeterIds =
  createSelector<Maybe<Gateway>, Maybe<Gateway>, uuid[]>(
    (gateway: Maybe<Gateway>) => gateway,
    (gateway: Maybe<Gateway>) => gateway.map((g) => g.meterIds).orElse([]),
  );
