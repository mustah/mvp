import {translate} from '../services/translationService';
import {GatewayDataSummaryKey} from '../state/domain-models/gateway/gatewayModels';
import {MeterDataSummaryKey, MeterStatus} from '../state/domain-models-paginated/meter/meterModels';
import {ParameterName} from '../state/search/selection/selectionModels';
import {IdNamed} from '../types/Types';

// TODO: Write tests on this.
export const getTranslationOrName = (idName: IdNamed, domainModelName: ParameterName): string => {
  switch (domainModelName) {
    case ParameterName.meterStatuses:
      return meterStatusTranslation(idName);
    default:
      return idName.name;
  }
};
export const meterStatusTranslation = ({id, name}: IdNamed): string => {
  const statuses = {
    [MeterStatus.ok]: translate('ok'),
    [MeterStatus.alarm]: translate('alarm'),
    [MeterStatus.unknown]: translate('unknown'),
  };
  return statuses[id] || name;
};
const flaggedTranslation = (text: string): string => {
  const texts = {
    flagged: translate('flagged'),
    unFlagged: translate('unFlagged'),
  };
  return texts[text] || text;
};
type MeterGatewaySummaryKey = MeterDataSummaryKey | GatewayDataSummaryKey;
export const pieChartTranslation = (fieldKey: MeterGatewaySummaryKey, toBeTranslated: IdNamed): string => {
  switch (fieldKey) {
    case 'flagged':
      return flaggedTranslation(toBeTranslated.name);
    case 'status':
      return meterStatusTranslation(toBeTranslated);
    default:
      return toBeTranslated.name;
  }
};
