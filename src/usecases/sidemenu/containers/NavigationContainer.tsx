import * as React from 'react';
import {Icon} from '../../common/components/icons/Icons';
import {Column} from '../../layouts/components/column/Column';
import {Row} from '../../layouts/components/row/Row';
import {NavigationItem} from '../components/NavigationItem';

export const NavigationContainer = props => (
  <Row className="Row-right">
    <Column>
      <NavigationItem name="Tillbaka" icon="undo"/>
    </Column>
    <Column>
      <NavigationItem name="Framåt" icon="redo"/>
    </Column>
    <Column>
      <Icon name="dots-horizontal"/>
    </Column>
  </Row>
);
