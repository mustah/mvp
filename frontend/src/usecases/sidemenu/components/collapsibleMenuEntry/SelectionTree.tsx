import List from 'material-ui/List/List';
import ListItem from 'material-ui/List/ListItem';
import ActionLineWeight from 'material-ui/svg-icons/action/line-weight';
import * as React from 'react';
import 'SelectionTree.scss';
import {translate} from '../../../../services/translationService';
import {selectionTreeData} from '../../models/organizedData';

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
      className="TreeListItem"
      primaryText={entity.name}
      key={id}
      style={selectionTreeItem.fontSize}
      innerDivStyle={selectionTreeItem.padding}
      initiallyOpen={false}
      nestedItems={entity.childNodes.ids.map(mapFnc)}
    />
  );
};

const selectionTreeItem = {
  fontSize: {fontSize: '14px'},
  padding: {padding: '5px 16px'},
};
