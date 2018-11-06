import {ListItem} from 'material-ui';
import * as React from 'react';
import {sideBarStyle} from '../../../../app/themes';
import ListItemProps = __MaterialUI.List.ListItemProps;

interface Selectable {
  selectable: boolean;
}

export const SelectableListItem = ({selectable, ...listItemProps}: ListItemProps & Selectable) => {
  if (selectable) {
    listItemProps.hoverColor = sideBarStyle.color;
  } else {
    listItemProps.disabled = true;
  }

  return <ListItem {...listItemProps}/>;
};
