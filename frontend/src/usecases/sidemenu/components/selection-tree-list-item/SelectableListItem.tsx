import {ListItem} from 'material-ui';
import * as React from 'react';
import {sideBarStyles} from '../../../../app/themes';
import ListItemProps = __MaterialUI.List.ListItemProps;

interface Selectable {
  selectable: boolean;
}

export const SelectableListItem = ({selectable, ...listItemProps}: ListItemProps & Selectable) => {
  if (selectable) {
    listItemProps.hoverColor = sideBarStyles.onHover.color;
  } else {
    listItemProps.disabled = true;
  }

  return <ListItem {...listItemProps}/>;
};
