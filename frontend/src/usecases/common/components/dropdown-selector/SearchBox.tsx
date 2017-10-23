import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';

export const SearchBox = (props) => {
  return (
    <div className="SearchBox">
      <input type="textfield" className="SearchBox-input"/>
      <ActionSearch style={{position: 'absolute', right: '12px', top: '5px', color: '#7b7b7b'}}/>
    </div>
  );
};
