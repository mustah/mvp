import {normalize, schema} from 'normalizr';
import {firstUpper} from '../../../../../services/translationService';
import {uuid} from '../../../../../types/Types';
import {LegendItem} from '../../../../../usecases/report/reportModels';
import {Normalized} from '../../../../domain-models/domainModels';
import {SelectedTreeEntities} from '../../../../selection-tree/selectionTreeModels';
import {isSelectedCity, isSelectedMeter} from '../measurementActions';

const lineSchema = [new schema.Entity('lines', {}, {idAttribute: 'id'})];

// TODO wrap as a selector in selectionTreeSelectors (just as getMedia())
export const selectedListItemsToLegendTable =
  ({
    selectedListItems,
    entities: {cities, meters},
  }: SelectedTreeEntities): Normalized<LegendItem> => {
    const cityLines: LegendItem[] = selectedListItems
      .filter(isSelectedCity)
      .map((id: uuid): LegendItem => {
        const {name, medium} = cities[id];
        return {
          id,
          city: name,
          medium,
        };
      });

    const meterLines: LegendItem[] = selectedListItems
      .filter(isSelectedMeter)
      .map((id: uuid): LegendItem => {
        const {name, medium, address, city} = meters[id];
        const cityParts = city.match(/[^,]+,(.+)/);
        const cityWithoutCountry = cityParts === null ? city : cityParts[1];
        return {
          id,
          facility: name,
          address,
          city: firstUpper(cityWithoutCountry),
          medium,
        };
      });

    return normalize([...cityLines, ...meterLines], lineSchema);
  };
