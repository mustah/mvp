import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {selectionTreeData} from '../../models/organizedData';
import ActionLineWeight from 'material-ui/svg-icons/action/line-weight';
import {translate} from '../../../../services/translationService';
import List from 'material-ui/List/List';

export const SelectionTree = (props) => {

  const cities = selectionTreeData.result.cities;
  const levelRelations = {cities: 'addresses', addresses: 'meteringPoints'};
  const renderSelectionOverview = (id) =>
    renderSelectionTree(id, levelRelations, 'cities', selectionTreeData);

  return (
    <List>
      <ListItem
        className="ListItem"
        primaryText={translate('selection overview')}
        leftIcon={<ActionLineWeight/>}
        initiallyOpen={false}
        nestedItems={[cities.map(renderSelectionOverview)]}
      />
    </List>
  );
};

export const renderSelectionTree = (id, levelRelations, level, data) => {
  const nextLevel = levelRelations[level];
  const mapFnc = (treeItem) => renderSelectionTree(treeItem, levelRelations, nextLevel, data);
  const entity = data.entities[level][id];
  const name = entity.name;
  const children = entity.childNodes;
  return (
    <ListItem
      className="ListItem"
      primaryText={name}
      key={id}
      initiallyOpen={false}
      nestedItems={children.map(mapFnc)}
    />
  );
};
