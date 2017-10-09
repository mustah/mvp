import * as React from 'react';
import {Column} from '../../common/components/layouts/column/Column';
import {Content} from '../../common/components/layouts/content/Content';
import {LinkItem} from '../components/linkitem/LinkItem';
import {NavigationContainer} from './NavigationContainer';
import {SearchContainer} from './SearchContainer';

export const SideMenuContainer = props => (
  <Column className="side-menu-container flex-1 flex-fill-horizontally">
    <NavigationContainer/>
    <SearchContainer/>
    <Content>
      <LinkItem name="Sparade objekt" icon="star"/>
      <LinkItem name="Sparade filter" icon="folder-star"/>
      <LinkItem name="Dynamiska grupper" icon="autorenew"/>
      <LinkItem name="Statiska grupper" icon="format-list-bulleted"/>
      <LinkItem name="Fullständig katalog" icon="home-modern"/>
    </Content>
  </Column>
);
