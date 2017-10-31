import * as React from 'react';
import {Column} from '../../common/components/layouts/column/Column';
import {CollapsibleMenu} from '../components/collapsibleMenuEntry/CollapsibleMenu';
import {organizedData} from './organizedData';

export class CatalogueCompleteContainer extends React.Component<any, {isOpen: boolean}> {
  state = {isOpen: true};

  render() {
    return (
      <Column >
        <CollapsibleMenu data={organizedData} hide={!this.state.isOpen}/>
      </Column>
    );
  }
}
