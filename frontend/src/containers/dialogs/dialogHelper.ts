import {normalize} from 'normalizr';
import {Medium} from '../../components/indicators/indicatorWidgetModels';
import {translate} from '../../services/translationService';
import {
  Gateway,
  GatewayStatusChangelog,
} from '../../state/domain-models-paginated/gateway/gatewayModels';
import {statusChangelogSchema} from '../../state/domain-models-paginated/gateway/gatewaySchema';
import {MeterStatusChangelog} from '../../state/domain-models-paginated/meter/meterModels';
import {measurement} from '../../state/domain-models-paginated/meter/meterSchema';
import {DomainModel, Normalized, ObjectsById} from '../../state/domain-models/domainModels';
import {Flag} from '../../state/domain-models/flag/flagModels';
import {allQuantities, Measurement} from '../../state/ui/graph/measurement/measurementModels';
import {uuid} from '../../types/Types';
import {RenderableMeasurement} from './MeterDetailsTabs';
import {MeterDetails} from '../../state/domain-models/meter-details/meterDetailsModels';

export const titleOf = (flags: Flag[]): string => {
  if (flags.length) {
    return flags.map((flag) => flag.title).join(', ');
  } else {
    return translate('no');
  }
};

type Changelogs = GatewayStatusChangelog | MeterStatusChangelog;

export const normalizedStatusChangelogFor = (domainModel: Gateway | MeterDetails): Normalized<Changelogs> => {
  const {entities, result} = normalize(domainModel, statusChangelogSchema);

  return {
    entities: entities.statusChangelog,
    result: Array.isArray(result.statusChangelog) ? result.statusChangelog : [],
  };
};

const orderedQuantities = (medium: string): string[] => {
  const translationTable: { [key: string]: Medium } = {
    'District heating': Medium.districtHeating,
    'Gas': Medium.gas,
    'Water': Medium.water,
    'Cold water': Medium.coldWater,
    'Hot water': Medium.hotWater,
    'Temperature inside': Medium.temperatureInside,
  };
  return medium in translationTable && translationTable[medium] in allQuantities
    ? allQuantities[translationTable[medium]]
    : [];
};

export const meterMeasurementsForTable = (meter: MeterDetails): DomainModel<RenderableMeasurement> => {
  const normalized: Normalized<Measurement> = normalize(meter.measurements, measurement);
  const entities: ObjectsById<RenderableMeasurement> =
    normalized.entities.measurements ? {...normalized.entities.measurements} : {};
  const result: uuid[] = normalized.result;
  const orderedResult: uuid[] = [];

  orderedQuantities(meter.medium).forEach((quantity) => {
    if (!result.includes(quantity)) {
      entities[quantity] = {
        id: quantity,
        quantity,
      };
    }
    orderedResult.push(quantity);
  });

  return {
    entities,
    result: orderedResult,
  };
};
