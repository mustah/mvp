import * as React from 'react';
import 'SearchContentBox.scss';
import {Column} from '../../common/components/layouts/column/Column';
import {Row} from '../../common/components/layouts/row/Row';
import {SearchDispatchToProps, SearchStateToProps} from '../containers/SearchContainer';
import {DropDownSelector} from './DropDownSelector';
import {SearchResultList} from './SearchResultList';

const regions = [
  {label: 'Norr'},
  {label: 'Väst'},
  {label: 'Söder'},
];

const cities = [
  {label: 'Göteborg'},
  {label: 'Stockholm'},
  {label: 'Malmö'},
  {label: 'Kungsbacka'},
];

const areas = [
  {label: 'Solna'},
  {label: 'Göteborg - Centrum'},
  {label: 'Mölndal'},
  {label: 'Kungsbacka'},
  {label: 'Kungsbacka - Centrum'},
];

const properties = [
  {label: 'Scandia'},
  {label: 'Wallenstam'},
  {label: 'Blockgatan'},
  {label: 'Peab'},
];

const appartments = [
  {label: 'Lägenhet 13'},
  {label: 'Lägenhet 2'},
  {label: 'Lägenhet 003'},
  {label: 'Lgh 4'},
];

const statuses = [
  {label: 'Ok'},
  {label: 'Varning'},
  {label: 'Info'},
  {label: 'Kritisk'},
];

const meteringPoints = [
  {label: 'UNICOcoder'},
  {label: '3100'},
  {label: 'Some other name'},
  {label: 'xxx222'},
];

export const SearchContentBox = (props: SearchStateToProps & SearchDispatchToProps) => {
  const {selectSearchOption} = props;
  return (
    <Column className="SearchContentBox">
      <Row>
        <DropDownSelector
          name="region"
          list={regions}
          selectionText="Region: Alla"
          onClick={selectSearchOption}
        />
        <DropDownSelector
          name="city"
          list={cities}
          selectionText="Stad: Alla"
          onClick={selectSearchOption}
        />
        <DropDownSelector
          name="area"
          list={areas}
          selectionText="Område: Alla"
          onClick={selectSearchOption}
        />
        <DropDownSelector
          name="property"
          list={properties}
          selectionText="Fastighet: Alla"
          onClick={selectSearchOption}
        />
        <DropDownSelector
          name="property"
          list={appartments}
          selectionText="Lägenhet: Alla"
          onClick={selectSearchOption}
        />
        <DropDownSelector
          name="status"
          list={statuses}
          selectionText="Status: Alla"
          onClick={selectSearchOption}
        />
        <DropDownSelector
          name="moid"
          list={meteringPoints}
          selectionText="Mätare: Alla"
          onClick={selectSearchOption}
        />
      </Row>

      <SearchResultList/>
    </Column>
  );
};
