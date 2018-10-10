import {normalize, schema} from 'normalizr';
import {createSelector} from 'reselect';
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

const regExp = /[^,]+,(.+)/;

const selectedCityLines = (selectedIds: uuid[], cities: ObjectsById<SelectionTreeCity>): LegendItem[] =>
  selectedIds
    .filter(isSelectedCity)
    .map((id: uuid): LegendItem => {
      const {name: city, medium} = cities[id];
      return {id, city, medium};
    });

const selectedMeterLines = (selectedIds: uuid[], meters: ObjectsById<SelectionTreeMeter>): LegendItem[] =>
  selectedIds
    .filter(isSelectedMeter)
    .map((id: uuid): LegendItem => {
      const {name: facility, medium, address, city} = meters[id];
      const cityParts = city.match(regExp);
      const cityWithoutCountry = cityParts === null ? city : cityParts[1];
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
