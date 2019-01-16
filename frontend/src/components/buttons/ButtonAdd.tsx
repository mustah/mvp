import RaisedButton from 'material-ui/RaisedButton';
import ContentAdd from 'material-ui/svg-icons/content/add';
import * as React from 'react';
import RaisedButtonProps = __MaterialUI.RaisedButtonProps;

const style: React.CSSProperties = {
  marginBottom: 16,
  marginLeft: 16,
};

export const ButtonAdd = (props: RaisedButtonProps) => (
  <RaisedButton
    primary={true}
    style={style}
    icon={<ContentAdd/>}
    {...props}
  />
);
