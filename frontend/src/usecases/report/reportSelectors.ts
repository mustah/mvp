import {normalize, schema} from 'normalizr';
import {createSelector} from 'reselect';
import {isDefined} from '../../helpers/commonUtils';
import {cityWithoutCountry} from '../../helpers/formatters';
import {RootState} from '../../reducers/rootReducer';
import {Normalized, ObjectsById} from '../../state/domain-models/domainModels';
import {
  SelectedTreeEntities,
  SelectionTreeCity,
  SelectionTreeEntities,
  SelectionTreeMeter,
} from '../../state/selection-tree/selectionTreeModels';
import {
  isSelectedCity,
  isSelectedMeter,
  MeasurementParameters
} from '../../state/ui/graph/measurement/measurementActions';
import {uuid} from '../../types/Types';
import {LegendItem} from './reportModels';

const lineSchema = [new schema.Entity('lines')];

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
    .map(({id, name: facility, medium, address, city}: SelectionTreeMeter) => ({
      id,
      facility,
      address,
      city: cityWithoutCountry(city),
      medium,
    }));

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

export const getMeasurementParameters =
  createSelector<RootState, RootState, MeasurementParameters>(
    (state) => state,
    ({
      report: {resolution, selectedListItems},
      userSelection: {userSelection: {selectionParameters}},
      selectionTree: {entities: selectionTreeEntities},
      ui: {indicator: {selectedQuantities: quantities}},
    }) => ({
      quantities,
      resolution,
      selectedListItems,
      selectionTreeEntities,
      selectionParameters,
    })
  );
