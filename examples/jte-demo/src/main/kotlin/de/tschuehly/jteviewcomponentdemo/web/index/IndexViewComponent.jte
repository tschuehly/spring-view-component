@import de.tschuehly.jteviewcomponentdemo.web.example.ExampleViewComponent
@import de.tschuehly.jteviewcomponentdemo.web.index.IndexViewComponent

@param IndexViewComponent.ExampleFormDTO exampleFormDTO
@param ExampleViewComponent exampleViewComponent

<script src="http://localhost:35729/livereload.js"></script>

<p>Index ViewComponent</p>

<h2>Say hello to ${exampleFormDTO.getExampleProp()}  </h2>


${exampleViewComponent.render()}