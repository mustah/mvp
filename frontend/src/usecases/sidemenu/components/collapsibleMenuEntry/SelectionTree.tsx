import ListItem from 'material-ui/List/ListItem';
import * as React from 'react';
import {selectionTreeData} from '../../models/organizedData';
import ActionLineWeight from 'material-ui/svg-icons/action/line-weight';
import {translate} from '../../../../services/translationService';
import List from 'material-ui/List/List';

interface SelectionTreeProps {
  topLvl: string;
}

export const SelectionTree = (props: SelectionTreeProps) => {
  const {topLvl} = props;
  const topLvlList = selectionTreeData.result[topLvl];
  const renderSelectionOverview = (id) =>
    renderSelectionTree(id, selectionTreeData, topLvl);

  return (
    <List>
      <ListItem
        className="ListItem"
        primaryText={translate('selection overview')}
        leftIcon={<ActionLineWeight/>}
        initiallyOpen={false}
        nestedItems={topLvlList.map(renderSelectionOverview)}
      />
    </List>
  );
};

export const renderSelectionTree = (id, data, level) => {
  const entity = data.entities[level][id];
  const nextLevel = entity.childNodes.type;

  const mapFnc = (treeItem) => renderSelectionTree(treeItem, data, nextLevel);

  return (
    <ListItem
      className="ListItem"
      primaryText={entity.name}
      key={id}
      initiallyOpen={false}
      nestedItems={entity.childNodes.ids.map(mapFnc)}
    />
  );
};
