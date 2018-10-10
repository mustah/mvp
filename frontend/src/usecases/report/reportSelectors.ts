import {normalize, schema} from 'normalizr';
import {createSelector} from 'reselect';
import {isDefined} from '../../helpers/commonUtils';
import {firstUpper} from '../../services/translationService';
import {Normalized, ObjectsById} from '../../state/domain-models/domainModels';
import {
  SelectedTreeEntities,
  SelectionTreeCity,
  SelectionTreeEntities,
  SelectionTreeMeter,
} from '../../state/selection-tree/selectionTreeModels';
import {isSelectedCity, isSelectedMeter} from '../../state/ui/graph/measurement/measurementActions';
import {uuid} from '../../types/Types';
import {LegendItem} from './reportModels';

const lineSchema = [new schema.Entity('lines')];

const matchesCityId = (id: string): RegExpMatchArray | null => id.match(/[^,]+,(.+)/);

const selectedCityLines = (selectedIds: uuid[], cities: ObjectsById<SelectionTreeCity>): LegendItem[] =>
  selectedIds
    .filter(isSelectedCity)
    .map((id: uuid) => cities[id])
    .filter(isDefined)
    .map(({id, name: city, medium}: SelectionTreeCity): LegendItem => ({id, city, medium}));

const selectedMeterLines = (selectedIds: uuid[], meters: ObjectsById<SelectionTreeMeter>): LegendItem[] =>
  selectedIds
    .filter(isSelectedMeter)
    .map((id: uuid) => meters[id])
    .filter(isDefined)
    .map(({id, name: facility, medium, address, city}: SelectionTreeMeter) => {
      const cityMatchParts = matchesCityId(city);
      const cityWithoutCountry = cityMatchParts === null ? city : cityMatchParts[1];
      return {
        id,
        facility,
        address,
        city: firstUpper(cityWithoutCountry),
        medium,
      };
    });

export const getLegendItems =
  createSelector<SelectedTreeEntities, uuid[], SelectionTreeEntities, Normalized<LegendItem>>(
    ({selectedListItems}) => selectedListItems,
    ({entities}) => entities,
    (selectedIds: uuid[], {cities, meters}: SelectionTreeEntities) => {
      const cityLines: LegendItem[] = selectedCityLines(selectedIds, cities);
      const meterLines: LegendItem[] = selectedMeterLines(selectedIds, meters);

      return normalize([...cityLines, ...meterLines], lineSchema);
    },
  );
