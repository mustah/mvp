import * as React from 'react';
import {Column} from '../../../components/layouts/column/Column';
import {MeterList} from '../../../components/metering-point/MeterList';
import {Normalized} from '../../../state/domain-models/domainModels';
import {Meter} from '../../../state/domain-models/meter/meterModels';
import {OnClickWithId} from '../../../types/Types';
import './SelectionResultList.scss';

interface Props {
  selectEntryAdd: OnClickWithId;
}

export const SearchResultList = (props: Normalized<Meter> & Props) => {
  const {selectEntryAdd} = props;
  return (
    <Column className="SearchResultList">
      <MeterList {...props} selectEntryAdd={selectEntryAdd}/>
    </Column>
  );
};
