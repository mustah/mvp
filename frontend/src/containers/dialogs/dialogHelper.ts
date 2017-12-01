import {normalize} from 'normalizr';
import {translate} from '../../services/translationService';
import {Normalized} from '../../state/domain-models/domainModels';
import {Flag} from '../../state/domain-models/flag/flagModels';
import {Gateway, GatewayStatusChangelog} from '../../state/domain-models/gateway/gatewayModels';
import {statusChangelogSchema} from '../../state/domain-models/gateway/gatewaySchema';

export const titleOf = (flags: Flag[]): string => {
  if (flags.length) {
    return flags.map((flag) => flag.title).join(', ');
  } else {
    return translate('no');
  }
};

export const normalizedStatusChangelogs = (gateway: Gateway): Normalized<GatewayStatusChangelog> => {
  const {entities, result} = normalize(gateway, statusChangelogSchema);
  return {
    entities: entities.statusChangelog,
    result: result.statusChangelog,
  };
};
