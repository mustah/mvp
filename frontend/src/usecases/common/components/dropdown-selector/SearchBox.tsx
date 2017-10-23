import ActionSearch from 'material-ui/svg-icons/action/search';
import * as React from 'react';

export const SearchBox = (props) => {
  return (
    <div className="SearchBox">
      <input type="textfield" className="SearchBox-input"/>
      <ActionSearch style={{position: 'absolute', right: '10px', top: '2px', color: '#7b7b7b'}}/>
    </div>
  );
};
