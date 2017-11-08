import FlatButton from 'material-ui/FlatButton';
import ActionInfoOutline from 'material-ui/svg-icons/action/info-outline';
import * as React from 'react';
import {OnClick} from '../../../../types/Types';

interface InfoLinkProps {
  onClick: OnClick;
  label: string;
}

export const InfoLink = (props: InfoLinkProps) => {
  const {label, onClick} = props;
  return (
    <FlatButton
      hoverColor="inherit"
      icon={<ActionInfoOutline/>}
      label={label}
      labelPosition="before"
      onClick={onClick}
    />
  );
};
