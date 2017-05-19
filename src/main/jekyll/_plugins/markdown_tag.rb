# Licenced under Creative Commons Attribution-ShareAlike 3.0 (cc-by-sa 3.0)
# See licenses/LICENSE-CC-BY-SA-3.0.txt for details
# Modified from code posted by http://stackoverflow.com/users/586033/breno-salgado on stackoverflow
# Question: http://stackoverflow.com/questions/7226076/in-jekyll-is-there-a-concise-way-to-render-a-markdown-partial
# Comment containing code: http://stackoverflow.com/a/14247353
# Modified to check if running jekyll from gradle (i.e. the top of the git repository) or running the stand-alone
# jekyll (i.e. from the jekyll theme directory)
module Jekyll
  class MarkdownTag < Liquid::Tag
    def initialize(tag_name, text, tokens)
      super
      @text = text.strip
    end
    require "kramdown"
    def render(context)
      if Dir.pwd.end_with?("jekyll")
        # running jekyll inside the top level directory of the jekyll theme directory
        tmpl = File.read File.join Dir.pwd, @text
      else
        # running jekyll using gradle
        tmpl = File.read File.join Dir.pwd, 'src/main/jekyll/', @text
      end
      Jekyll::Converters::Markdown::KramdownParser.new(Jekyll.configuration()).convert(tmpl)
    end
  end
end
Liquid::Template.register_tag('markdown', Jekyll::MarkdownTag)
