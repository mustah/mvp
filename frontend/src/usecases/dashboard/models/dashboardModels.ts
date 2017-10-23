import {Indicator} from '../../common/components/indicators/models/IndicatorModels';
import {uuid} from '../../../types/Types';

export interface SystemOverviewState {
  title: string;
  indicators: Indicator[];
}

export interface DashboardModel {
  id: uuid;
  title: string;
  author: string;
  systemOverview: SystemOverviewState;
}

export const normalizedData = {
  meteringPoints: {
    byId: {
      '1234 1234 1234': {
        id: '1234 1234 1234',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 0,
          text: 'ok',
        },
      },
      '1234 1234 1235': {
        id: '1234 1234 1235',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 2,
          text: 'Batteri lågt',
        },
      },
      '1234 1234 1236': {
        id: '1234 1234 1236',
        type: 'UNICOcoder',
        location: 'Område 1 fast 12',
        gateway: 'YY',
        status: {
          code: 3,
          text: 'Batteri slut',
        },
      },
    },
    allIds: ['1234 1234 1234', '1234 1234 1235', '1234 1234 1236'],
  },
};
